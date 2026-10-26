import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { parkingApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { Card, LoadingSpinner, EmptyState, Badge } from '../components/ui'

export function ParkingAvailability() {
  const { user } = useAuth()
  const isStaff = user?.role === 'ADMINISTRATOR' || user?.role === 'PARKING_ATTENDANT'
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const res = await parkingApi.getAllSlots()
      setSlots(res.data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  if (loading) return <LoadingSpinner label="Loading parking slots…" />
  if (error) return <div className="alert alert-error">{error}</div>

  const available = slots.filter(s => s.status === 'AVAILABLE').length
  const occupied = slots.filter(s => s.status === 'OCCUPIED').length
  const reserved = slots.filter(s => s.status === 'RESERVED').length
  const outOfService = slots.filter(s => s.status === 'OUT_OF_SERVICE').length

  const statusClass = (status) => status.toLowerCase().replace(/_/g, '_')

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Parking Availability</h1>
          <p>Real-time slot status across all floors</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}>↻ Refresh</button>
      </div>

      <div className="stats-grid">
        <div className="stat-card"><div className="stat-label">Available</div><div className="stat-value" style={{ color: 'var(--success)' }}>{available}</div></div>
        <div className="stat-card"><div className="stat-label">Occupied</div><div className="stat-value" style={{ color: 'var(--danger)' }}>{occupied}</div></div>
        <div className="stat-card"><div className="stat-label">Reserved</div><div className="stat-value" style={{ color: 'var(--warning)' }}>{reserved}</div></div>
        <div className="stat-card"><div className="stat-label">Out of Service</div><div className="stat-value" style={{ color: 'var(--gray-400)' }}>{outOfService}</div></div>
      </div>

      {slots.length === 0 ? (
        <EmptyState title="No parking slots configured" message="Slots will appear here once configured." />
      ) : (
        <div className="slot-grid">
          {slots.map(slot => (
            <div key={slot.id} className={`slot-card ${statusClass(slot.status)}`}>
              <div className="slot-number">{slot.slotNumber}</div>
              <div className="slot-type">{slot.slotType} · Floor {slot.floor}</div>
              <div className="slot-status">{slot.status.replace(/_/g, ' ')}</div>
            </div>
          ))}
        </div>
      )}

      {user?.role === 'CUSTOMER' && available > 0 && (
        <div className="mt-2 text-center">
          <Link to="/parking/session" className="btn btn-primary">Park My Vehicle</Link>
        </div>
      )}
    </div>
  )
}
