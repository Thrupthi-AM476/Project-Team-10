import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { parkingApi, vehicleApi } from '../api/client'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, ConfirmDialog, Field,
} from '../components/ui'

const SLOT_STATUSES = ['AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE']
const SLOT_TYPES = ['REGULAR', 'DISABLED', 'EV']
const vehicleIsActive = vehicle => Boolean(vehicle.isActive ?? vehicle.active)

export function StaffDashboard() {
  const [slots, setSlots] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [showSlotForm, setShowSlotForm] = useState(false)
  const [editingSlot, setEditingSlot] = useState(null)
  const [slotForm, setSlotForm] = useState({
    slotNumber: '', slotType: 'REGULAR', floor: 'G', status: '',
  })
  const [saving, setSaving] = useState(false)
  const [confirmDeactivate, setConfirmDeactivate] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [slotResponse, vehicleResponse] = await Promise.all([
        parkingApi.getAllSlots(),
        vehicleApi.list(),
      ])
      setSlots(slotResponse.data || [])
      setVehicles(vehicleResponse.data || [])
    } catch (err) {
      setError(err.message || 'Unable to load the staff dashboard.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const openAddSlot = () => {
    setEditingSlot(null)
    setSlotForm({ slotNumber: '', slotType: 'REGULAR', floor: 'G', status: '' })
    setError('')
    setShowSlotForm(true)
  }

  const openEditSlot = slot => {
    setEditingSlot(slot)
    setSlotForm({
      slotNumber: slot.slotNumber,
      slotType: slot.slotType,
      floor: slot.floor || 'G',
      status: slot.status,
    })
    setError('')
    setShowSlotForm(true)
  }

  const handleSlotSubmit = async event => {
    event.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)
    try {
      if (editingSlot) {
        await parkingApi.updateSlot(editingSlot.id, slotForm)
        setSuccess('Slot updated successfully.')
      } else {
        await parkingApi.createSlot(slotForm)
        setSuccess('Slot created successfully.')
      }
      setShowSlotForm(false)
      await load()
    } catch (err) {
      setError(err.message || 'Unable to save the slot.')
    } finally {
      setSaving(false)
    }
  }

  const handleDeactivateSlot = async () => {
    if (!confirmDeactivate) return
    setError('')
    setSuccess('')
    try {
      await parkingApi.deactivateSlot(confirmDeactivate.id)
      setSuccess(`Slot ${confirmDeactivate.slotNumber} marked out of service.`)
      setConfirmDeactivate(null)
      await load()
    } catch (err) {
      setError(err.message || 'Unable to disable the slot.')
      setConfirmDeactivate(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading staff dashboard…" />

  const available = slots.filter(slot => slot.status === 'AVAILABLE').length
  const occupied = slots.filter(slot => slot.status === 'OCCUPIED').length
  const reserved = slots.filter(slot => slot.status === 'RESERVED').length
  const outOfService = slots.filter(slot => slot.status === 'OUT_OF_SERVICE').length

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Staff Dashboard</h1>
          <p>Manage parking slots and oversee registered vehicles</p>
        </div>
        <div className="flex gap">
          <Link to="/parking" className="btn btn-secondary btn-sm">Parking View</Link>
          <button className="btn btn-primary btn-sm" onClick={openAddSlot}>+ Add Slot</button>
        </div>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      <div className="stats-grid">
        <div className="stat-card"><div className="stat-label">Total Slots</div><div className="stat-value">{slots.length}</div></div>
        <div className="stat-card"><div className="stat-label">Available</div><div className="stat-value" style={{ color: 'var(--success)' }}>{available}</div></div>
        <div className="stat-card"><div className="stat-label">Occupied</div><div className="stat-value" style={{ color: 'var(--danger)' }}>{occupied}</div></div>
        <div className="stat-card"><div className="stat-label">Reserved</div><div className="stat-value" style={{ color: 'var(--warning)' }}>{reserved}</div></div>
        <div className="stat-card"><div className="stat-label">Out of Service</div><div className="stat-value" style={{ color: 'var(--gray-500)' }}>{outOfService}</div></div>
        <div className="stat-card"><div className="stat-label">Vehicles</div><div className="stat-value">{vehicles.length}</div></div>
      </div>

      {showSlotForm && (
        <Card className="mb-2">
          <h3 className="mb-1">{editingSlot ? 'Edit Slot' : 'Add Parking Slot'}</h3>
          <form onSubmit={handleSlotSubmit}>
            <div className="grid-2">
              <Field label="Slot Number" htmlFor="slot-number">
                <input
                  id="slot-number"
                  type="text"
                  value={slotForm.slotNumber}
                  onChange={event => setSlotForm(prev => ({ ...prev, slotNumber: event.target.value.toUpperCase() }))}
                  required
                  disabled={!!editingSlot}
                  placeholder="C-01"
                />
              </Field>
              <Field label="Slot Type" htmlFor="slot-type-form">
                <select
                  id="slot-type-form"
                  value={slotForm.slotType}
                  onChange={event => setSlotForm(prev => ({ ...prev, slotType: event.target.value }))}
                >
                  {SLOT_TYPES.map(type => <option key={type} value={type}>{type}</option>)}
                </select>
              </Field>
              <Field label="Floor" htmlFor="slot-floor">
                <input
                  id="slot-floor"
                  type="text"
                  value={slotForm.floor}
                  onChange={event => setSlotForm(prev => ({ ...prev, floor: event.target.value }))}
                  placeholder="G"
                />
              </Field>
              {editingSlot && (
                <Field label="Status" htmlFor="slot-status">
                  <select
                    id="slot-status"
                    value={slotForm.status}
                    onChange={event => setSlotForm(prev => ({ ...prev, status: event.target.value }))}
                  >
                    {SLOT_STATUSES.map(status => <option key={status} value={status}>{status.replace(/_/g, ' ')}</option>)}
                  </select>
                </Field>
              )}
            </div>
            <div className="flex gap">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Save'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => setShowSlotForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </Card>
      )}

      <Card className="mb-2">
        <h3 className="mb-1">Parking Slots</h3>
        {slots.length === 0 ? (
          <EmptyState title="No slots" message="Add a parking slot to get started." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Slot</th><th>Type</th><th>Floor</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {slots.map(slot => (
                  <tr key={slot.id}>
                    <td><strong>{slot.slotNumber}</strong></td>
                    <td>{slot.slotType}</td>
                    <td>{slot.floor}</td>
                    <td>
                      <Badge variant={
                        slot.status === 'AVAILABLE' ? 'success'
                          : slot.status === 'OCCUPIED' ? 'danger'
                            : slot.status === 'RESERVED' ? 'warning'
                              : 'default'
                      }>{slot.status.replace(/_/g, ' ')}</Badge>
                    </td>
                    <td>
                      <div className="flex gap">
                        <button className="btn btn-sm btn-secondary" onClick={() => openEditSlot(slot)}>Edit</button>
                        {slot.status !== 'OUT_OF_SERVICE' && (
                          <button className="btn btn-sm btn-danger" onClick={() => setConfirmDeactivate(slot)}>
                            Disable
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Card>
        <h3 className="mb-1">All Vehicles</h3>
        {vehicles.length === 0 ? (
          <EmptyState title="No vehicles" message="No vehicles have been registered." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Plate</th><th>Type</th><th>Owner</th><th>Status</th></tr></thead>
              <tbody>
                {vehicles.map(vehicle => {
                  const active = vehicleIsActive(vehicle)
                  return (
                    <tr key={vehicle.id}>
                      <td><strong>{vehicle.licensePlate}</strong></td>
                      <td>{vehicle.vehicleType}</td>
                      <td>{vehicle.ownerName || `User #${vehicle.userId}`}</td>
                      <td>
                        <Badge variant={active ? 'success' : 'default'}>
                          {active ? 'Active' : 'Inactive'}
                        </Badge>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <ConfirmDialog
        open={!!confirmDeactivate}
        title="Disable Slot"
        message={`Mark slot ${confirmDeactivate?.slotNumber} as out of service?`}
        onConfirm={handleDeactivateSlot}
        onCancel={() => setConfirmDeactivate(null)}
        confirmLabel="Disable"
      />
    </div>
  )
}
