import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { vehicleApi, parkingApi } from '../api/client'
import { Card, LoadingSpinner, EmptyState, Badge, ErrorMessage } from '../components/ui'

const VEHICLE_TYPE_ICON = { CAR: '🚗', MOTORCYCLE: '🏍', TRUCK: '🚚', EV: '⚡' }

function timeSince(dateStr) {
  const now = Date.now()
  const then = new Date(dateStr).getTime()
  const mins = Math.floor((now - then) / 60000)
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  return `${Math.floor(hrs / 24)}d ago`
}

export function CustomerDashboard() {
  const { user } = useAuth()
  const [vehicles, setVehicles] = useState([])
  const [activeSession, setActiveSession] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      setLoading(true)
      try {
        const [vRes, sRes] = await Promise.all([
          vehicleApi.list(),
          parkingApi.getActiveSession(),
        ])
        setVehicles(vRes.data || [])
        const sessions = sRes.data || []
        setActiveSession(sessions.length > 0 ? sessions[0] : null)
      } catch (err) {
        setError(err.message || 'Failed to load dashboard data.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <LoadingSpinner label="Loading dashboard…" />

  const activeVehicles = vehicles.filter(v => v.isActive).length

  return (
    <main>
      {/* Greeting header */}
      <div className="page-header flex-between" style={{ flexWrap: 'wrap', gap: '.5rem' }}>
        <div>
          <h1>Good day, {user?.fullName?.split(' ')[0]} 👋</h1>
          <p>Here's an overview of your parking activity</p>
        </div>
        <div className="flex-gap-sm">
          <Link to="/parking/session" className="btn btn-primary btn-sm">
            🅿 Park Now
          </Link>
        </div>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}

      {/* Stats row */}
      <div className="stats-grid">
        <div className="stat-card">
          <span className="stat-icon">🚗</span>
          <div className="stat-label">My Vehicles</div>
          <div className="stat-value">{vehicles.length}</div>
        </div>
        <div className="stat-card">
          <span className="stat-icon">✅</span>
          <div className="stat-label">Active</div>
          <div className="stat-value">{activeVehicles}</div>
        </div>
        <div className="stat-card">
          <span className="stat-icon">🅿</span>
          <div className="stat-label">Parked now</div>
          <div className="stat-value">{activeSession ? '1' : '0'}</div>
        </div>
      </div>

      <div className="grid-2 mb-2">
        {/* Active session card */}
        <Card>
          <p className="section-title">Active Parking Session</p>
          {activeSession ? (
            <div>
              <div className="info-list">
                <div className="info-row">
                  <span className="info-label">Ticket ID</span>
                  <span className="info-value" style={{ fontFamily: 'monospace', fontSize: '.82rem' }}>
                    {activeSession.ticketId}
                  </span>
                </div>
                <div className="info-row">
                  <span className="info-label">Vehicle</span>
                  <span className="info-value">{activeSession.licensePlate}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Slot</span>
                  <Badge variant="primary">{activeSession.slotNumber}</Badge>
                </div>
                <div className="info-row">
                  <span className="info-label">Parked</span>
                  <span className="info-value">{timeSince(activeSession.entryTime)}</span>
                </div>
              </div>
              <Link to="/parking/exit" className="btn btn-danger btn-sm w-full mt-1">
                Exit & Pay →
              </Link>
            </div>
          ) : (
            <EmptyState
              title="No active session"
              message="Park a vehicle to see its session here."
              action={
                <Link to="/parking/session" className="btn btn-primary btn-sm">
                  Park a vehicle
                </Link>
              }
            />
          )}
        </Card>

        {/* Vehicles preview */}
        <Card>
          <div className="flex-between mb-1">
            <p className="section-title" style={{ marginBottom: 0 }}>My Vehicles</p>
            <Link to="/vehicles" className="text-sm text-primary font-medium">
              View all →
            </Link>
          </div>
          {vehicles.length === 0 ? (
            <EmptyState
              title="No vehicles yet"
              message="Register your first vehicle to start parking."
              action={
                <Link to="/vehicles" className="btn btn-primary btn-sm">
                  Add Vehicle
                </Link>
              }
            />
          ) : (
            <div>
              {vehicles.slice(0, 4).map(v => (
                <div key={v.id} className="info-row">
                  <div className="flex align-center" style={{ gap: '.5rem' }}>
                    <span style={{ fontSize: '1.1rem' }}>{VEHICLE_TYPE_ICON[v.vehicleType] || '🚗'}</span>
                    <div>
                      <strong style={{ fontSize: '.9rem' }}>{v.licensePlate}</strong>
                      {(v.make || v.model) && (
                        <div className="text-xs text-gray">{[v.make, v.model].filter(Boolean).join(' ')}</div>
                      )}
                    </div>
                  </div>
                  <Badge variant={v.isActive ? 'success' : 'default'}>
                    {v.isActive ? 'Active' : 'Inactive'}
                  </Badge>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* Quick actions */}
      <Card>
        <p className="section-title">Quick Actions</p>
        <div className="flex-gap mt-1">
          <Link to="/vehicles"        className="btn btn-secondary btn-sm">🚗 Vehicles</Link>
          <Link to="/parking"         className="btn btn-secondary btn-sm">🅿 Availability</Link>
          <Link to="/parking/session" className="btn btn-secondary btn-sm">🎫 Park Vehicle</Link>
          <Link to="/parking/exit"    className="btn btn-secondary btn-sm">🚪 Exit</Link>
          <Link to="/reservations"    className="btn btn-secondary btn-sm">📅 Reservations</Link>
          <Link to="/profile"         className="btn btn-secondary btn-sm">👤 Profile</Link>
        </div>
      </Card>
    </main>
  )
}
