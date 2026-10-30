import { useEffect, useState } from 'react'
import { vehicleApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, ConfirmDialog,
} from '../components/ui'

const VEHICLE_TYPES = ['CAR', 'MOTORCYCLE', 'TRUCK', 'EV']
const TYPE_ICON     = { CAR: '🚗', MOTORCYCLE: '🏍', TRUCK: '🚚', EV: '⚡' }
const TYPE_LABEL    = { CAR: 'Car', MOTORCYCLE: 'Motorcycle', TRUCK: 'Truck', EV: 'Electric' }

const PLATE_RE = /^[A-Z0-9\-]{2,20}$/

function VehicleForm({ initial, onSave, onCancel }) {
  const [form, setForm]   = useState(initial)
  const [err, setErr]     = useState('')
  const [saving, setSaving] = useState(false)
  const [touched, setTouched] = useState({})

  const plateError = touched.licensePlate && !PLATE_RE.test(form.licensePlate)
    ? 'Plate must be 2–20 uppercase letters, digits or hyphens'
    : ''

  const set   = f => e => setForm(p => ({ ...p, [f]: e.target.value }))
  const touch = f => () => setTouched(p => ({ ...p, [f]: true }))

  const handleSubmit = async e => {
    e.preventDefault()
    setErr('')
    setTouched({ licensePlate: true })
    if (!PLATE_RE.test(form.licensePlate)) return
    setSaving(true)
    try {
      await onSave(form)
    } catch (ex) {
      setErr(ex.message || 'Could not save vehicle.')
    } finally {
      setSaving(false)
    }
  }

  const isEdit = !!initial.id

  return (
    <form onSubmit={handleSubmit} noValidate aria-label={isEdit ? 'Edit vehicle' : 'Register vehicle'}>
      {err && <ErrorMessage message={err} onDismiss={() => setErr('')} />}

      <div className="grid-2">
        <div className="field">
          <label htmlFor="vf-plate">
            License plate <span aria-hidden="true" style={{ color: 'var(--danger)' }}>*</span>
          </label>
          <input
            id="vf-plate"
            type="text"
            value={form.licensePlate}
            onChange={e => setForm(p => ({ ...p, licensePlate: e.target.value.toUpperCase() }))}
            onBlur={touch('licensePlate')}
            required
            placeholder="KA01AB1234"
            className={plateError ? 'input-error' : ''}
            aria-required="true"
            aria-describedby={plateError ? 'err-plate' : 'hint-plate'}
            maxLength={20}
            autoComplete="off"
          />
          {plateError
            ? <span id="err-plate"  className="field-error" role="alert">{plateError}</span>
            : <span id="hint-plate" className="field-hint">Format: KA01AB1234 (2–20 chars)</span>
          }
        </div>

        <div className="field">
          <label htmlFor="vf-type">Vehicle type</label>
          <select
            id="vf-type"
            value={form.vehicleType}
            onChange={set('vehicleType')}
          >
            {VEHICLE_TYPES.map(t => (
              <option key={t} value={t}>{TYPE_ICON[t]} {TYPE_LABEL[t]}</option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="vf-make">Make</label>
          <input
            id="vf-make" type="text" value={form.make}
            onChange={set('make')} placeholder="Toyota"
            autoComplete="off"
          />
        </div>

        <div className="field">
          <label htmlFor="vf-model">Model</label>
          <input
            id="vf-model" type="text" value={form.model}
            onChange={set('model')} placeholder="Corolla"
            autoComplete="off"
          />
        </div>

        <div className="field">
          <label htmlFor="vf-color">Colour</label>
          <input
            id="vf-color" type="text" value={form.color}
            onChange={set('color')} placeholder="White"
            autoComplete="off"
          />
        </div>
      </div>

      <div className="flex-gap-sm mt-1">
        <button type="submit" className="btn btn-primary" disabled={saving} aria-busy={saving}>
          {saving ? (
            <>
              <span className="spinner" style={{ width: 14, height: 14, borderWidth: 2 }} aria-hidden="true" />
              Saving…
            </>
          ) : isEdit ? 'Update Vehicle' : 'Register Vehicle'}
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  )
}

export function Vehicles() {
  const { user } = useAuth()
  const isStaff = user?.role === 'ADMINISTRATOR' || user?.role === 'PARKING_ATTENDANT'

  const [vehicles, setVehicles]           = useState([])
  const [loading, setLoading]             = useState(true)
  const [error, setError]                 = useState('')
  const [success, setSuccess]             = useState('')
  const [showForm, setShowForm]           = useState(false)
  const [editing, setEditing]             = useState(null)
  const [confirmDelete, setConfirmDelete] = useState(null)

  const EMPTY_FORM = { licensePlate: '', make: '', model: '', color: '', vehicleType: 'CAR' }

  const load = async () => {
    setLoading(true)
    try {
      const res = await vehicleApi.list()
      setVehicles(res.data || [])
    } catch (err) {
      setError(err.message || 'Could not load vehicles.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const openAdd = () => { setEditing(null);  setShowForm(true); setSuccess('') }
  const openEdit = v => { setEditing(v);     setShowForm(true); setSuccess('') }
  const closeForm = () => { setShowForm(false); setEditing(null) }

  const handleSave = async form => {
    if (editing) {
      await vehicleApi.update(editing.id, form)
      setSuccess(`Vehicle ${form.licensePlate} updated.`)
    } else {
      await vehicleApi.create(form)
      setSuccess(`Vehicle ${form.licensePlate} registered.`)
    }
    closeForm()
    await load()
  }

  const handleDeactivate = async () => {
    if (!confirmDelete) return
    try {
      await vehicleApi.deactivate(confirmDelete.id)
      setSuccess(`Vehicle ${confirmDelete.licensePlate} deactivated.`)
      await load()
    } catch (err) {
      setError(err.message || 'Could not deactivate vehicle.')
    } finally {
      setConfirmDelete(null)
    }
  }

  if (loading) return <LoadingSpinner label="Loading vehicles…" />

  return (
    <main>
      <div className="page-header flex-between" style={{ flexWrap: 'wrap', gap: '.5rem' }}>
        <div>
          <h1>Vehicles</h1>
          <p>{isStaff ? 'All registered vehicles' : 'Your registered vehicles'}</p>
        </div>
        {!isStaff && !showForm && (
          <button className="btn btn-primary" onClick={openAdd}>
            + Add Vehicle
          </button>
        )}
      </div>

      {error   && <ErrorMessage message={error}   onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {showForm && (
        <Card className="mb-2">
          <h3 className="mb-1">{editing ? `Edit — ${editing.licensePlate}` : 'Register New Vehicle'}</h3>
          <VehicleForm
            initial={editing
              ? { licensePlate: editing.licensePlate, make: editing.make || '',
                  model: editing.model || '', color: editing.color || '',
                  vehicleType: editing.vehicleType, id: editing.id }
              : EMPTY_FORM}
            onSave={handleSave}
            onCancel={closeForm}
          />
        </Card>
      )}

      {vehicles.length === 0 ? (
        <Card>
          <EmptyState
            title="No vehicles registered"
            message="Register your first vehicle to start using the parking system."
            action={
              !isStaff && (
                <button className="btn btn-primary" onClick={openAdd}>
                  Register a Vehicle
                </button>
              )
            }
          />
        </Card>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th scope="col">License Plate</th>
                <th scope="col">Type</th>
                <th scope="col">Make / Model</th>
                <th scope="col">Colour</th>
                {isStaff && <th scope="col">Owner</th>}
                <th scope="col">Status</th>
                {!isStaff && <th scope="col">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {vehicles.map(v => (
                <tr key={v.id}>
                  <td>
                    <strong style={{ fontFamily: 'monospace', letterSpacing: '.04em' }}>
                      {v.licensePlate}
                    </strong>
                  </td>
                  <td>
                    <span title={TYPE_LABEL[v.vehicleType]}>
                      {TYPE_ICON[v.vehicleType] || '🚗'}{' '}
                      <span className="text-sm">{TYPE_LABEL[v.vehicleType] || v.vehicleType}</span>
                    </span>
                  </td>
                  <td>
                    {v.make || v.model
                      ? `${v.make || ''} ${v.model || ''}`.trim()
                      : <span className="text-muted">—</span>
                    }
                  </td>
                  <td>{v.color || <span className="text-muted">—</span>}</td>
                  {isStaff && (
                    <td className="text-sm">{v.ownerName}</td>
                  )}
                  <td>
                    <Badge variant={v.isActive ? 'success' : 'default'}>
                      {v.isActive ? 'Active' : 'Inactive'}
                    </Badge>
                  </td>
                  {!isStaff && (
                    <td>
                      <div className="flex-gap-sm">
                        <button
                          className="btn btn-ghost btn-sm"
                          onClick={() => openEdit(v)}
                          aria-label={`Edit vehicle ${v.licensePlate}`}
                        >
                          ✏️ Edit
                        </button>
                        {v.isActive && (
                          <button
                            className="btn btn-ghost btn-sm"
                            style={{ color: 'var(--danger)' }}
                            onClick={() => setConfirmDelete(v)}
                            aria-label={`Deactivate vehicle ${v.licensePlate}`}
                          >
                            🗑 Deactivate
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        open={!!confirmDelete}
        title="Deactivate Vehicle"
        message={`Are you sure you want to deactivate ${confirmDelete?.licensePlate}? It will no longer appear as active.`}
        onConfirm={handleDeactivate}
        onCancel={() => setConfirmDelete(null)}
        confirmLabel="Yes, Deactivate"
      />
    </main>
  )
}
