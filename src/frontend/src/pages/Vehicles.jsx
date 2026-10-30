import { useEffect, useState } from 'react'
import { vehicleApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, ConfirmDialog, Field,
} from '../components/ui'

const VEHICLE_TYPES = ['CAR', 'MOTORCYCLE', 'TRUCK', 'EV']

export function Vehicles() {
  const { user } = useAuth()
  const isStaff = user?.role === 'ADMINISTRATOR' || user?.role === 'PARKING_ATTENDANT'
  const [vehicles, setVehicles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Form state
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({
    licensePlate: '', make: '', model: '', color: '', vehicleType: 'CAR',
  })
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  // Deletion confirm
  const [confirmDelete, setConfirmDelete] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const res = await vehicleApi.list()
      setVehicles(res.data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const openAdd = () => {
    setEditing(null)
    setForm({ licensePlate: '', make: '', model: '', color: '', vehicleType: 'CAR' })
    setFormError('')
    setShowForm(true)
  }

  const openEdit = (v) => {
    setEditing(v)
    setForm({
      licensePlate: v.licensePlate, make: v.make || '', model: v.model || '',
      color: v.color || '', vehicleType: v.vehicleType,
    })
    setFormError('')
    setShowForm(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormError('')
    if (form.licensePlate.trim().length < 2) {
      setFormError('License plate is required (min 2 characters)')
      return
    }
    setSaving(true)
    try {
      if (editing) {
        await vehicleApi.update(editing.id, form)
        setSuccess('Vehicle updated successfully')
      } else {
        await vehicleApi.create(form)
        setSuccess('Vehicle registered successfully')
      }
      setShowForm(false)
      await load()
    } catch (err) {
      setFormError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDeactivate = async () => {
    if (!confirmDelete) return
    try {
      await vehicleApi.deactivate(confirmDelete.id)
      setSuccess('Vehicle deactivated')
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setConfirmDelete(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading vehicles…" />

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Vehicles</h1>
          <p>{isStaff ? 'All registered vehicles' : 'Your registered vehicles'}</p>
        </div>
        {!isStaff && (
          <button className="btn btn-primary" onClick={openAdd}>+ Add Vehicle</button>
        )}
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {showForm && (
        <Card className="mb-2">
          <h3 className="mb-1">{editing ? 'Edit Vehicle' : 'Register New Vehicle'}</h3>
          {formError && <ErrorMessage message={formError} onDismiss={() => setFormError('')} />}
          <form onSubmit={handleSubmit}>
            <div className="grid-2">
              <Field label="License Plate" htmlFor="plate">
                <input id="plate" type="text" value={form.licensePlate}
                  onChange={e => setForm(prev => ({ ...prev, licensePlate: e.target.value.toUpperCase() }))}
                  required placeholder="KA01AB1234" />
              </Field>
              <Field label="Vehicle Type" htmlFor="type">
                <select id="type" value={form.vehicleType}
                  onChange={e => setForm(prev => ({ ...prev, vehicleType: e.target.value }))}>
                  {VEHICLE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </Field>
              <Field label="Make" htmlFor="make">
                <input id="make" type="text" value={form.make}
                  onChange={e => setForm(prev => ({ ...prev, make: e.target.value }))}
                  placeholder="Toyota" />
              </Field>
              <Field label="Model" htmlFor="model">
                <input id="model" type="text" value={form.model}
                  onChange={e => setForm(prev => ({ ...prev, model: e.target.value }))}
                  placeholder="Corolla" />
              </Field>
              <Field label="Color" htmlFor="color">
                <input id="color" type="text" value={form.color}
                  onChange={e => setForm(prev => ({ ...prev, color: e.target.value }))}
                  placeholder="White" />
              </Field>
            </div>
            <div className="flex gap mt-1">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving…' : editing ? 'Update' : 'Register'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </Card>
      )}

      {vehicles.length === 0 ? (
        <EmptyState title="No vehicles found" message="Register a vehicle to get started."
          action={!isStaff ? <button className="btn btn-primary" onClick={openAdd}>Add Vehicle</button> : null} />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Plate</th><th>Type</th><th>Make</th><th>Model</th>
                <th>Color</th>{isStaff && <th>Owner</th>}<th>Status</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.map(v => (
                <tr key={v.id}>
                  <td><strong>{v.licensePlate}</strong></td>
                  <td>{v.vehicleType}</td>
                  <td>{v.make || '—'}</td>
                  <td>{v.model || '—'}</td>
                  <td>{v.color || '—'}</td>
                  {isStaff && <td>{v.ownerName}</td>}
                  <td><Badge variant={v.isActive ? 'success' : 'default'}>{v.isActive ? 'Active' : 'Inactive'}</Badge></td>
                  <td>
                    {!isStaff && (
                      <div className="flex gap">
                        <button className="btn btn-sm btn-secondary" onClick={() => openEdit(v)}>Edit</button>
                        {v.isActive && <button className="btn btn-sm btn-danger" onClick={() => setConfirmDelete(v)}>Deactivate</button>}
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        open={!!confirmDelete}
        title="Deactivate Vehicle"
        message={`Deactivate vehicle ${confirmDelete?.licensePlate}? This will mark it as inactive.`}
        onConfirm={handleDeactivate}
        onCancel={() => setConfirmDelete(null)}
        confirmLabel="Deactivate"
      />
    </div>
  )
}
