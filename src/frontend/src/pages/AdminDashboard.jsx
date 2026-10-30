import { useEffect, useState } from 'react'
import { adminApi } from '../api/client'
import {
  Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage,
  Badge, Field,
} from '../components/ui'

const inr = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' })
const formatINR = (v) => inr.format(Number(v ?? 0))

// <input type="datetime-local"> yields "YYYY-MM-DDTHH:mm" (local time). The API expects a
// zone-less LocalDateTime, so send it as-is instead of converting to UTC with toISOString().
const toApiDateTime = (v) => (v && v.length === 16 ? `${v}:00` : v)

export function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null)
  const [reports, setReports] = useState([])
  const [rates, setRates] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Report filter
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  // Rate form
  const [showRateForm, setShowRateForm] = useState(false)
  const [editingRate, setEditingRate] = useState(null)
  const [rateForm, setRateForm] = useState({
    name: '', vehicleType: 'CAR', ratePerHour: '', gracePeriodMinutes: 15, active: true,
  })
  const [saving, setSaving] = useState(false)

  const loadDashboard = async () => {
    try {
      const res = await adminApi.getDashboard()
      setDashboard(res.data)
    } catch (err) { setError(err.message) }
  }

  const loadRates = async () => {
    try {
      const res = await adminApi.getRates()
      setRates(res.data || [])
    } catch (err) { setError(err.message) }
  }

  const loadReports = async () => {
    if (fromDate && toDate && fromDate > toDate) {
      setError('"From" must be earlier than "To"')
      return
    }
    try {
      const res = await adminApi.getReports(
        fromDate ? toApiDateTime(fromDate) : null,
        toDate ? toApiDateTime(toDate) : null,
      )
      setReports(res.data || [])
    } catch (err) { setError(err.message) }
  }

  useEffect(() => {
    if (!success) return undefined
    const t = setTimeout(() => setSuccess(''), 4000)
    return () => clearTimeout(t)
  }, [success])

  useEffect(() => {
    async function load() {
      await Promise.all([loadDashboard(), loadRates(), loadReports()])
      setLoading(false)
    }
    load()
  }, [])

  const handleRateSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload = {
        ...rateForm,
        ratePerHour: parseFloat(rateForm.ratePerHour),
      }
      if (editingRate) {
        await adminApi.updateRate(editingRate.id, payload)
        setSuccess('Rate schedule updated')
      } else {
        await adminApi.createRate(payload)
        setSuccess('Rate schedule created')
      }
      setShowRateForm(false)
      setEditingRate(null)
      setRateForm({ name: '', vehicleType: 'CAR', ratePerHour: '', gracePeriodMinutes: 15, active: true })
      await loadRates()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const openAddRate = () => {
    setEditingRate(null)
    setRateForm({ name: '', vehicleType: 'CAR', ratePerHour: '', gracePeriodMinutes: 15, active: true })
    setShowRateForm(true)
  }

  const openEditRate = (r) => {
    setEditingRate(r)
    setRateForm({
      name: r.name, vehicleType: r.vehicleType,
      ratePerHour: String(r.ratePerHour), gracePeriodMinutes: r.gracePeriodMinutes,
      active: r.active,
    })
    setShowRateForm(true)
  }

  if (loading) return <LoadingSpinner label="Loading admin dashboard…" />

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Administrator Dashboard</h1>
          <p>System overview, reports, and rate configuration</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={openAddRate}>+ Add Rate</button>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {dashboard && (
        <div className="stats-grid">
          <div className="stat-card"><div className="stat-label">Total Slots</div><div className="stat-value">{dashboard.totalSlots}</div></div>
          <div className="stat-card"><div className="stat-label">Available</div><div className="stat-value" style={{ color: 'var(--success)' }}>{dashboard.availableSlots}</div></div>
          <div className="stat-card"><div className="stat-label">Occupied</div><div className="stat-value" style={{ color: 'var(--danger)' }}>{dashboard.occupiedSlots}</div></div>
          <div className="stat-card"><div className="stat-label">Active Parked</div><div className="stat-value">{dashboard.activeVehicles}</div></div>
          <div className="stat-card"><div className="stat-label">Registered Users</div><div className="stat-value">{dashboard.totalUsersRegistered}</div></div>
          <div className="stat-card"><div className="stat-label">Completed Txns</div><div className="stat-value">{dashboard.totalCompletedTransactions}</div></div>
          <div className="stat-card"><div className="stat-label">Revenue Today</div><div className="stat-value" style={{ color: 'var(--success)' }}>{formatINR(dashboard.revenueToday)}</div></div>
          <div className="stat-card"><div className="stat-label">Revenue (Month)</div><div className="stat-value" style={{ color: 'var(--success)' }}>{formatINR(dashboard.revenueThisMonth)}</div></div>
        </div>
      )}

      {showRateForm && (
        <Card className="mb-2">
          <h3 className="mb-1">{editingRate ? 'Edit Rate Schedule' : 'Create Rate Schedule'}</h3>
          <form onSubmit={handleRateSubmit}>
            <div className="grid-2">
              <Field label="Rate Name" htmlFor="rate-name">
                <input id="rate-name" type="text" value={rateForm.name}
                  onChange={e => setRateForm(prev => ({ ...prev, name: e.target.value }))} required
                  placeholder="Standard - Car" />
              </Field>
              <Field label="Vehicle Type" htmlFor="rate-vtype">
                <select id="rate-vtype" value={rateForm.vehicleType}
                  onChange={e => setRateForm(prev => ({ ...prev, vehicleType: e.target.value }))}>
                  <option value="CAR">Car</option>
                  <option value="MOTORCYCLE">Motorcycle</option>
                  <option value="TRUCK">Truck</option>
                  <option value="EV">EV</option>
                </select>
              </Field>
              <Field label="Rate per Hour (₹)" htmlFor="rate-amount">
                <input id="rate-amount" type="number" step="0.01" min="0.01" value={rateForm.ratePerHour}
                  onChange={e => setRateForm(prev => ({ ...prev, ratePerHour: e.target.value }))} required
                  placeholder="30.00" />
              </Field>
              <Field label="Grace Period (minutes)" htmlFor="rate-grace">
                <input id="rate-grace" type="number" min="0" value={rateForm.gracePeriodMinutes}
                  onChange={e => setRateForm(prev => ({ ...prev, gracePeriodMinutes: parseInt(e.target.value) || 0 }))} />
              </Field>
            </div>
            <label htmlFor="rate-active" style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', margin: '0.5rem 0 1rem' }}>
              <input id="rate-active" type="checkbox" checked={!!rateForm.active}
                onChange={e => setRateForm(prev => ({ ...prev, active: e.target.checked }))} />
              Active (only one active schedule is allowed per vehicle type)
            </label>
            <div className="flex gap">
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
              <button type="button" className="btn btn-secondary" onClick={() => setShowRateForm(false)}>Cancel</button>
            </div>
          </form>
        </Card>
      )}

      <Card className="mb-2">
        <h3 className="mb-1">Rate Schedules (VPS-F-014)</h3>
        {rates.length === 0 ? (
          <EmptyState title="No rate schedules" message="Create a rate schedule to enable fee calculation." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Name</th><th>Vehicle Type</th><th>Rate/Hr</th><th>Grace (min)</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {rates.map(r => (
                  <tr key={r.id}>
                    <td>{r.name}</td>
                    <td>{r.vehicleType}</td>
                    <td>{formatINR(r.ratePerHour)}</td>
                    <td>{r.gracePeriodMinutes}</td>
                    <td><Badge variant={r.active ? 'success' : 'default'}>{r.active ? 'Active' : 'Inactive'}</Badge></td>
                    <td><button className="btn btn-sm btn-secondary" onClick={() => openEditRate(r)}>Edit</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Card>
        <h3 className="mb-1">Parking History Report (VPS-F-015)</h3>
        <div className="flex gap mb-1" style={{ flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <Field label="From" htmlFor="report-from">
            <input id="report-from" type="datetime-local" value={fromDate}
              onChange={e => setFromDate(e.target.value)} />
          </Field>
          <Field label="To" htmlFor="report-to">
            <input id="report-to" type="datetime-local" value={toDate}
              onChange={e => setToDate(e.target.value)} />
          </Field>
          <button className="btn btn-primary btn-sm" onClick={loadReports}>Generate Report</button>
        </div>

        {reports.length === 0 ? (
          <EmptyState title="No records found" message="No parking history matches the selected date range." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Ticket</th><th>Plate</th><th>Slot</th><th>Entry</th><th>Exit</th><th>Duration</th><th>Fee</th><th>Payment</th></tr></thead>
              <tbody>
                {reports.map(r => (
                  <tr key={r.id}>
                    <td>{r.ticketId}</td>
                    <td>{r.licensePlate}</td>
                    <td>{r.slotNumber}</td>
                    <td>{r.entryTime ? new Date(r.entryTime).toLocaleString() : '—'}</td>
                    <td>{r.exitTime ? new Date(r.exitTime).toLocaleString() : '—'}</td>
                    <td>{r.durationMinutes != null ? `${r.durationMinutes}m` : '—'}</td>
                    <td>{r.feeAmount != null ? formatINR(r.feeAmount) : '—'}</td>
                    <td><Badge variant={r.paymentStatus === 'SUCCESS' ? 'success' : 'warning'}>{r.paymentStatus || '—'}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}
