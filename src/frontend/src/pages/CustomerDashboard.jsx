import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { vehicleApi, parkingApi } from '../api/client'
import { Card, LoadingSpinner, EmptyState, Badge } from '../components/ui'

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
        setError(err.message || 'Failed to load dashboard')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <LoadingSpinner label="Loading dashboard…" />
  if (error) return <div className="alert alert-error">{error}</div>

  return (
    <div>
      <div className="page-header">
        <h1>Welcome, {user?.fullName}</h1>
        <p>Manage your vehicles, parking sessions, and reservations</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">My Vehicles</div>
          <div className="stat-value">{vehicles.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Active Session</div>
          <div className="stat-value">{activeSession ? 'Yes' : 'None'}</div>
        </div>
      </div>

      <div className="grid-2 mb-2">
        <Card>
          <h3 className="mb-1">My Vehicles</h3>
          {vehicles.length === 0 ? (
            <EmptyState title="No vehicles yet" message="Register your first vehicle to start parking."
              action={<Link to="/vehicles" className="btn btn-primary btn-sm">Add Vehicle</Link>} />
          ) : (
            <>
              {vehicles.slice(0, 3).map(v => (
                <div key={v.id} className="flex-between mb-1">
                  <div>
                    <strong>{v.licensePlate}</strong>
                    <span className="text-sm text-gray"> · {v.make} {v.model}</span>
                  </div>
                  <Badge variant={v.isActive ? 'success' : 'default'}>
                    {v.isActive ? 'Active' : 'Inactive'}
                  </Badge>
                </div>
              ))}
              {vehicles.length > 3 && (
                <Link to="/vehicles" className="text-sm">View all {vehicles.length} →</Link>
              )}
            </>
          )}
        </Card>

        <Card>
          <h3 className="mb-1">Active Parking Session</h3>
          {activeSession ? (
            <div>
              <p><strong>Ticket:</strong> {activeSession.ticketId}</p>
              <p><strong>Plate:</strong> {activeSession.licensePlate}</p>
              <p><strong>Slot:</strong> {activeSession.slotNumber}</p>
              <p><strong>Entry:</strong> {new Date(activeSession.entryTime).toLocaleString()}</p>
              <Link to="/parking/exit" className="btn btn-primary btn-sm mt-1">Process Exit</Link>
            </div>
          ) : (
            <EmptyState title="No active session" message="Park your vehicle to start a session."
              action={<Link to="/parking" className="btn btn-primary btn-sm">Park Vehicle</Link>} />
          )}
        </Card>
      </div>

      <div className="grid-2">
        <Card>
          <h3 className="mb-1">Quick Actions</h3>
          <div className="flex gap mt-1" style={{ flexWrap: 'wrap' }}>
            <Link to="/vehicles" className="btn btn-secondary btn-sm">Manage Vehicles</Link>
            <Link to="/parking" className="btn btn-secondary btn-sm">Parking Availability</Link>
            <Link to="/parking/session" className="btn btn-secondary btn-sm">Parking Entry</Link>
            <Link to="/reservations" className="btn btn-secondary btn-sm">Reservations</Link>
          </div>
        </Card>

        <Card>
          <h3 className="mb-1">Account</h3>
          <p className="text-sm text-gray">Email: {user?.email}</p>
          <p className="text-sm text-gray">Role: {user?.role?.replace(/_/g, ' ').toLowerCase()}</p>
          <Link to="/profile" className="btn btn-secondary btn-sm mt-1">View Profile</Link>
        </Card>
      </div>
    </div>
  )
}
