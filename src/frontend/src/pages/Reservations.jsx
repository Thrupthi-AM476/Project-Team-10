import { useEffect, useState } from 'react'
import { reservationApi, vehicleApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, ConfirmDialog, Field,
} from '../components/ui'

export function Reservations() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMINISTRATOR'
  const [reservations, setReservations] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Form state
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    vehicleId: '', startTime: '', endTime: '', slotType: '',
  })
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [confirmCancel, setConfirmCancel] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const res = await reservationApi.list()
      setReservations(res.data || [])
      if (!isAdmin) {
        const vRes = await vehicleApi.list()
        setVehicles((vRes.data || []).filter(v => v.isActive))
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormError('')
    if (!form.vehicleId) { setFormError('Select a vehicle'); return }
    if (!form.startTime || !form.endTime) { setFormError('Select start and end times'); return }
    setSaving(true)
    try {
      // Convert datetime-local to ISO
      const payload = {
        vehicleId: Number(form.vehicleId),
        startTime: new Date(form.startTime).toISOString(),
        endTime: new Date(form.endTime).toISOString(),
        slotType: form.slotType || undefined,
      }
      await reservationApi.create(payload)
      setSuccess('Reservation created successfully')
      setShowForm(false)
      setForm({ vehicleId: '', startTime: '', endTime: '', slotType: '' })
      await load()
    } catch (err) {
      setFormError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleCancel = async () => {
    if (!confirmCancel) return
    try {
      await reservationApi.cancel(confirmCancel.id)
      setSuccess('Reservation cancelled')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
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
        {!isAdmin && (
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ New Reservation'}
          </button>
        )}
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {showForm && (
        <Card className="mb-2">
          <h3 className="mb-1">Create Reservation</h3>
          {formError && <ErrorMessage message={formError} onDismiss={() => setFormError('')} />}
          <form onSubmit={handleSubmit}>
            <Field label="Vehicle" htmlFor="res-vehicle">
              <select id="res-vehicle" value={form.vehicleId}
                onChange={e => setForm(prev => ({ ...prev, vehicleId: e.target.value }))} required>
                <option value="">— Choose a vehicle —</option>
                {vehicles.map(v => (
                  <option key={v.id} value={v.id}>{v.licensePlate} — {v.make} {v.model}</option>
                ))}
              </select>
            </Field>
            <div className="grid-2">
              <Field label="Start Time" htmlFor="res-start">
                <input id="res-start" type="datetime-local" value={form.startTime}
                  onChange={e => setForm(prev => ({ ...prev, startTime: e.target.value }))} required />
              </Field>
              <Field label="End Time" htmlFor="res-end">
                <input id="res-end" type="datetime-local" value={form.endTime}
                  onChange={e => setForm(prev => ({ ...prev, endTime: e.target.value }))} required />
              </Field>
            </div>
            <Field label="Preferred Slot Type (optional)" htmlFor="res-slot">
              <select id="res-slot" value={form.slotType}
                onChange={e => setForm(prev => ({ ...prev, slotType: e.target.value }))}>
                <option value="">Any available</option>
                <option value="REGULAR">Regular</option>
                <option value="DISABLED">Disabled</option>
                <option value="EV">EV</option>
              </select>
            </Field>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Creating…' : 'Create Reservation'}
            </button>
          </form>
        </Card>
      )}

      {reservations.length === 0 ? (
        <EmptyState title="No reservations" message="Create a reservation to book a slot in advance."
          action={!isAdmin ? <button className="btn btn-primary" onClick={() => setShowForm(true)}>New Reservation</button> : null} />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th><th>Vehicle</th><th>Slot</th>
                <th>Start</th><th>End</th><th>Status</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map(r => (
                <tr key={r.id}>
                  <td>#{r.id}</td>
                  <td>{r.licensePlate}</td>
                  <td>{r.slotNumber || '—'}</td>
                  <td>{new Date(r.startTime).toLocaleString()}</td>
                  <td>{new Date(r.endTime).toLocaleString()}</td>
                  <td><Badge variant={
                    r.status === 'CONFIRMED' ? 'success'
                    : r.status === 'CANCELLED' ? 'danger'
                    : r.status === 'EXPIRED' ? 'warning'
                    : r.status === 'COMPLETED' ? 'primary'
                    : 'default'
                  }>{r.status}</Badge></td>
                  <td>
                    {(r.status === 'PENDING' || r.status === 'CONFIRMED') && !isAdmin && (
                      <button className="btn btn-sm btn-danger" onClick={() => setConfirmCancel(r)}>Cancel</button>
                    )}
                  </td>
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
