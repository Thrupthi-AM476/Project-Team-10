import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { parkingApi, vehicleApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, ConfirmDialog, Field,
} from '../components/ui'

const SLOT_STATUSES = ['AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE']
const SLOT_TYPES = ['REGULAR', 'DISABLED', 'EV']

export function StaffDashboard() {
  const { user } = useAuth()
  const [slots, setSlots] = useState([])
  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Slot form
  const [showSlotForm, setShowSlotForm] = useState(false)
  const [editingSlot, setEditingSlot] = useState(null)
  const [slotForm, setSlotForm] = useState({ slotNumber: '', slotType: 'REGULAR', floor: 'G', status: '' })
  const [saving, setSaving] = useState(false)
  const [confirmDeactivate, setConfirmDeactivate] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const [sRes, vRes] = await Promise.all([
        parkingApi.getAllSlots(),
        vehicleApi.list(),
      ])
      setSlots(sRes.data || [])
      setVehicles(vRes.data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const openAddSlot = () => {
    setEditingSlot(null)
    setSlotForm({ slotNumber: '', slotType: 'REGULAR', floor: 'G', status: '' })
    setShowSlotForm(true)
  }

  const openEditSlot = (s) => {
    setEditingSlot(s)
    setSlotForm({ slotNumber: s.slotNumber, slotType: s.slotType, floor: s.floor, status: s.status })
    setShowSlotForm(true)
  }

  const handleSlotSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (editingSlot) {
        await parkingApi.updateSlot(editingSlot.id, slotForm)
        setSuccess('Slot updated')
      } else {
        await parkingApi.createSlot(slotForm)
        setSuccess('Slot created')
      }
      setShowSlotForm(false)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDeactivateSlot = async () => {
    if (!confirmDeactivate) return
    try {
      await parkingApi.deactivateSlot(confirmDeactivate.id)
      setSuccess('Slot marked out of service')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setConfirmDeactivate(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading staff dashboard…" />

  const available = slots.filter(s => s.status === 'AVAILABLE').length

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Staff Dashboard</h1>
          <p>Manage parking slots, vehicles, and parking operations</p>
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
        <div className="stat-card"><div className="stat-label">Occupied</div><div className="stat-value" style={{ color: 'var(--danger)' }}>{slots.filter(s => s.status === 'OCCUPIED').length}</div></div>
        <div className="stat-card"><div className="stat-label">Vehicles</div><div className="stat-value">{vehicles.length}</div></div>
      </div>

      {showSlotForm && (
        <Card className="mb-2">
          <h3 className="mb-1">{editingSlot ? 'Edit Slot' : 'Add Parking Slot'}</h3>
          <form onSubmit={handleSlotSubmit}>
            <div className="grid-2">
              <Field label="Slot Number" htmlFor="slot-number">
                <input id="slot-number" type="text" value={slotForm.slotNumber}
                  onChange={e => setSlotForm(prev => ({ ...prev, slotNumber: e.target.value.toUpperCase() }))}
                  required disabled={!!editingSlot} placeholder="C-01" />
              </Field>
              <Field label="Slot Type" htmlFor="slot-type-form">
                <select id="slot-type-form" value={slotForm.slotType}
                  onChange={e => setSlotForm(prev => ({ ...prev, slotType: e.target.value }))}>
                  {SLOT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </Field>
              <Field label="Floor" htmlFor="slot-floor">
                <input id="slot-floor" type="text" value={slotForm.floor}
                  onChange={e => setSlotForm(prev => ({ ...prev, floor: e.target.value }))} placeholder="G" />
              </Field>
              {editingSlot && (
                <Field label="Status" htmlFor="slot-status">
                  <select id="slot-status" value={slotForm.status}
                    onChange={e => setSlotForm(prev => ({ ...prev, status: e.target.value }))}>
                    {SLOT_STATUSES.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
                  </select>
                </Field>
              )}
            </div>
            <div className="flex gap">
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
              <button type="button" className="btn btn-secondary" onClick={() => setShowSlotForm(false)}>Cancel</button>
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
                {slots.map(s => (
                  <tr key={s.id}>
                    <td><strong>{s.slotNumber}</strong></td>
                    <td>{s.slotType}</td>
                    <td>{s.floor}</td>
                    <td><Badge variant={
                      s.status === 'AVAILABLE' ? 'success'
                      : s.status === 'OCCUPIED' ? 'danger'
                      : s.status === 'RESERVED' ? 'warning'
                      : 'default'
                    }>{s.status.replace(/_/g, ' ')}</Badge></td>
                    <td>
                      <div className="flex gap">
                        <button className="btn btn-sm btn-secondary" onClick={() => openEditSlot(s)}>Edit</button>
                        {s.status !== 'OUT_OF_SERVICE' && (
                          <button className="btn btn-sm btn-danger" onClick={() => setConfirmDeactivate(s)}>Disable</button>
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
                {vehicles.map(v => (
                  <tr key={v.id}>
                    <td><strong>{v.licensePlate}</strong></td>
                    <td>{v.vehicleType}</td>
                    <td>{v.ownerName}</td>
                    <td><Badge variant={v.isActive ? 'success' : 'default'}>{v.isActive ? 'Active' : 'Inactive'}</Badge></td>
                  </tr>
                ))}
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
