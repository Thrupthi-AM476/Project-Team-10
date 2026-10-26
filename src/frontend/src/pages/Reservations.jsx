import { useCallback, useEffect, useState } from 'react'
import { reservationApi, vehicleApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, ConfirmDialog, Field,
} from '../components/ui'

const vehicleIsActive = vehicle => Boolean(vehicle.isActive ?? vehicle.active)

export function Reservations() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMINISTRATOR'
  const canCreate = Boolean(user?.id) && user?.role !== 'ADMINISTRATOR'

  const [reservations, setReservations] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    vehicleId: '', startTime: '', endTime: '', slotType: '',
  })
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [confirmCancel, setConfirmCancel] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await reservationApi.list()
      setReservations(res.data || [])

      if (canCreate) {
        const vehicleResponse = await vehicleApi.list()
        setVehicles((vehicleResponse.data || []).filter(vehicle =>
          Number(vehicle.userId) === Number(user.id) && vehicleIsActive(vehicle)))
      } else {
        setVehicles([])
      }
    } catch (err) {
      setError(err.message || 'Unable to load reservations.')
    } finally {
      setLoading(false)
    }
  }, [canCreate, user?.id])

  useEffect(() => {
    load()
  }, [load])

  const handleSubmit = async (event) => {
    event.preventDefault()
    setFormError('')
    setError('')
    setSuccess('')

    if (!form.vehicleId) {
      setFormError('Select a vehicle.')
      return
    }
    if (!form.startTime || !form.endTime) {
      setFormError('Select both start and end times.')
      return
    }

    const start = new Date(form.startTime)
    const end = new Date(form.endTime)
    const now = new Date()

    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
      setFormError('Enter valid start and end times.')
      return
    }
    if (start <= now) {
      setFormError('Start time must be in the future.')
      return
    }
    if (end <= start) {
      setFormError('End time must be after start time.')
      return
    }

    setSaving(true)
    try {
      // datetime-local values are already local ISO date-times. Keep them
      // timezone-free because the backend DTO uses Java LocalDateTime.
      const payload = {
        vehicleId: Number(form.vehicleId),
        startTime: `${form.startTime}:00`,
        endTime: `${form.endTime}:00`,
        ...(form.slotType ? { slotType: form.slotType } : {}),
      }
      await reservationApi.create(payload)
      setSuccess('Reservation created successfully.')
      setShowForm(false)
      setForm({ vehicleId: '', startTime: '', endTime: '', slotType: '' })
      await load()
    } catch (err) {
      setFormError(err.message || 'Unable to create the reservation.')
    } finally {
      setSaving(false)
    }
  }

  const handleCancel = async () => {
    if (!confirmCancel) return
    setError('')
    setSuccess('')
    try {
      await reservationApi.cancel(confirmCancel.id)
      setSuccess('Reservation cancelled.')
      setConfirmCancel(null)
      await load()
    } catch (err) {
      setError(err.message || 'Unable to cancel the reservation.')
      setConfirmCancel(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading reservations…" />

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Reservations</h1>
          <p>{isAdmin ? 'All reservations' : 'Your parking reservations'}</p>
        </div>
        {canCreate && (
          <button
            className="btn btn-primary"
            onClick={() => {
              setShowForm(current => !current)
              setFormError('')
              setError('')
            }}
          >
            {showForm ? 'Close Form' : '+ New Reservation'}
          </button>
        )}
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {showForm && canCreate && (
        <Card className="mb-2">
          <h3 className="mb-1">Create Reservation</h3>
          {formError && <ErrorMessage message={formError} onDismiss={() => setFormError('')} />}
          <form onSubmit={handleSubmit}>
            <Field label="Vehicle" htmlFor="res-vehicle">
              <select
                id="res-vehicle"
                value={form.vehicleId}
                onChange={event => setForm(prev => ({ ...prev, vehicleId: event.target.value }))}
                required
              >
                <option value="">— Choose a vehicle —</option>
                {vehicles.map(vehicle => (
                  <option key={vehicle.id} value={vehicle.id}>
                    {vehicle.licensePlate} — {vehicle.make || ''} {vehicle.model || ''}
                  </option>
                ))}
              </select>
              {vehicles.length === 0 && (
                <span className="text-sm text-gray">Register an active vehicle before creating a reservation.</span>
              )}
            </Field>

            <div className="grid-2">
              <Field label="Start Time" htmlFor="res-start">
                <input
                  id="res-start"
                  type="datetime-local"
                  value={form.startTime}
                  onChange={event => setForm(prev => ({ ...prev, startTime: event.target.value }))}
                  required
                />
              </Field>
              <Field label="End Time" htmlFor="res-end">
                <input
                  id="res-end"
                  type="datetime-local"
                  value={form.endTime}
                  min={form.startTime || undefined}
                  onChange={event => setForm(prev => ({ ...prev, endTime: event.target.value }))}
                  required
                />
              </Field>
            </div>

            <Field label="Preferred Slot Type (optional)" htmlFor="res-slot">
              <select
                id="res-slot"
                value={form.slotType}
                onChange={event => setForm(prev => ({ ...prev, slotType: event.target.value }))}
              >
                <option value="">Any available</option>
                <option value="REGULAR">Regular</option>
                <option value="DISABLED">Disabled</option>
                <option value="EV">EV</option>
              </select>
            </Field>

            <div className="flex gap">
              <button type="submit" className="btn btn-primary" disabled={saving || vehicles.length === 0}>
                {saving ? 'Creating…' : 'Create Reservation'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </Card>
      )}

      {reservations.length === 0 ? (
        <EmptyState
          title="No reservations"
          message={isAdmin ? 'No reservations have been created yet.' : 'Create a reservation to book a slot in advance.'}
          action={canCreate ? (
            <button className="btn btn-primary" onClick={() => setShowForm(true)}>
              New Reservation
            </button>
          ) : null}
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                {isAdmin && <th>User</th>}
                <th>Vehicle</th><th>Slot</th><th>Start</th><th>End</th><th>Status</th>
                {!isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {reservations.map(reservation => (
                <tr key={reservation.id}>
                  <td>#{reservation.id}</td>
                  {isAdmin && <td>{reservation.userName || `User #${reservation.userId}`}</td>}
                  <td>{reservation.licensePlate}</td>
                  <td>{reservation.slotNumber || '—'}</td>
                  <td>{new Date(reservation.startTime).toLocaleString()}</td>
                  <td>{new Date(reservation.endTime).toLocaleString()}</td>
                  <td>
                    <Badge variant={
                      reservation.status === 'CONFIRMED' ? 'success'
                        : reservation.status === 'CANCELLED' ? 'danger'
                          : reservation.status === 'EXPIRED' ? 'warning'
                            : reservation.status === 'COMPLETED' ? 'primary'
                              : 'default'
                    }>
                      {reservation.status}
                    </Badge>
                  </td>
                  {!isAdmin && (
                    <td>
                      {(reservation.status === 'PENDING' || reservation.status === 'CONFIRMED') && (
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => setConfirmCancel(reservation)}
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        open={!!confirmCancel}
        title="Cancel Reservation"
        message={`Cancel reservation #${confirmCancel?.id} for ${confirmCancel?.licensePlate}?`}
        onConfirm={handleCancel}
        onCancel={() => setConfirmCancel(null)}
        confirmLabel="Cancel Reservation"
      />
    </div>
  )
}
