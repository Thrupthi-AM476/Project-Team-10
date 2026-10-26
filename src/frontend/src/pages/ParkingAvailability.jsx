import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { parkingApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { EmptyState, ErrorMessage, LoadingSpinner } from '../components/ui'

export function ParkingAvailability() {
  const { user } = useAuth()
  const [slots, setSlots] = useState([])
  const [availableSlotIds, setAvailableSlotIds] = useState(new Set())
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true)
    else setLoading(true)
    setError('')

    try {
      const [allResponse, availableResponse] = await Promise.all([
        parkingApi.getAllSlots(),
        parkingApi.getAvailableSlots(),
      ])
      const allSlots = allResponse.data || []
      const available = availableResponse.data || []
      setSlots(allSlots)
      setAvailableSlotIds(new Set(available.map(slot => String(slot.id))))
    } catch (err) {
      setError(err.message || 'Unable to load parking slots.')
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const displaySlots = useMemo(() => slots.map(slot => {
    // A slot with database status AVAILABLE can still be reserved for now.
    const isReservedNow = slot.status === 'AVAILABLE'
      && !availableSlotIds.has(String(slot.id))
    return { ...slot, displayStatus: isReservedNow ? 'RESERVED' : slot.status }
  }), [slots, availableSlotIds])

  const counts = useMemo(() => displaySlots.reduce((result, slot) => {
    const status = slot.displayStatus
    result.total += 1
    if (status === 'AVAILABLE') result.available += 1
    else if (status === 'OCCUPIED') result.occupied += 1
    else if (status === 'RESERVED') result.reserved += 1
    else if (status === 'OUT_OF_SERVICE') result.outOfService += 1
    return result
  }, { total: 0, available: 0, occupied: 0, reserved: 0, outOfService: 0 }), [displaySlots])

  const floorGroups = useMemo(() => {
    const groups = {}
    for (const slot of displaySlots) {
      const floor = slot.floor || 'Unassigned'
      if (!groups[floor]) groups[floor] = []
      groups[floor].push(slot)
    }
    for (const group of Object.values(groups)) {
      group.sort((a, b) => String(a.slotNumber).localeCompare(String(b.slotNumber), undefined, { numeric: true }))
    }
    return groups
  }, [displaySlots])

  if (loading) return <LoadingSpinner label="Loading parking slots…" />

  const statusClass = status => String(status).toLowerCase()
  const isCustomer = user?.role === 'CUSTOMER'

  return (
    <div>
      <div className="page-header flex-between">
        <div>
          <h1>Parking Availability</h1>
          <p>Live slot status across all floors</p>
        </div>
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => load(true)}
          disabled={refreshing}
        >
          {refreshing ? 'Refreshing…' : '↻ Refresh'}
        </button>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}

      <div className="stats-grid">
        <div className="stat-card"><div className="stat-label">Total Slots</div><div className="stat-value">{counts.total}</div></div>
        <div className="stat-card"><div className="stat-label">Available</div><div className="stat-value" style={{ color: 'var(--success)' }}>{counts.available}</div></div>
        <div className="stat-card"><div className="stat-label">Occupied</div><div className="stat-value" style={{ color: 'var(--danger)' }}>{counts.occupied}</div></div>
        <div className="stat-card"><div className="stat-label">Reserved</div><div className="stat-value" style={{ color: 'var(--warning)' }}>{counts.reserved}</div></div>
        <div className="stat-card"><div className="stat-label">Out of Service</div><div className="stat-value" style={{ color: 'var(--gray-500)' }}>{counts.outOfService}</div></div>
      </div>

      {error && slots.length === 0 ? null : slots.length === 0 ? (
        <EmptyState title="No parking slots configured" message="Slots will appear here once configured." />
      ) : (
        Object.keys(floorGroups).sort((a, b) => a.localeCompare(b, undefined, { numeric: true })).map(floor => (
          <section key={floor} className="mb-2" aria-label={`Parking slots on floor ${floor}`}>
            <h2 className="mb-1">Floor {floor}</h2>
            <div className="slot-grid">
              {floorGroups[floor].map(slot => (
                <div key={slot.id} className={`slot-card ${statusClass(slot.displayStatus)}`}>
                  <div className="slot-number">{slot.slotNumber}</div>
                  <div className="slot-type">{slot.slotType}</div>
                  <div className="slot-status">{slot.displayStatus.replace(/_/g, ' ')}</div>
                </div>
              ))}
            </div>
          </section>
        ))
      )}

      {isCustomer && counts.available > 0 && (
        <div className="mt-2 text-center">
          <Link to="/parking/session" className="btn btn-primary">Park My Vehicle</Link>
        </div>
      )}
    </div>
  )
}
