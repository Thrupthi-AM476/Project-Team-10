import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Landing() {
  const { isAuthenticated, user } = useAuth()
  const dashboardPath =
    user?.role === 'ADMINISTRATOR' ? '/admin'
    : user?.role === 'PARKING_ATTENDANT' || user?.role === 'MAINTENANCE_TECHNICIAN' ? '/staff'
    : '/dashboard'

  const features = [
    { icon: '🚗', title: 'Vehicle Management', desc: 'Register and manage your vehicles with license plate tracking' },
    { icon: '🅿', title: 'Smart Parking', desc: 'Real-time slot availability and automatic allocation' },
    { icon: '💳', title: 'Fee & Payment', desc: 'Automatic fee calculation based on duration and rate schedules' },
    { icon: '📅', title: 'Reservations', desc: 'Book parking slots in advance with conflict prevention' },
    { icon: '📊', title: 'Admin Dashboard', desc: 'Monitor occupancy, revenue, and generate reports' },
    { icon: '🔒', title: 'Secure Access', desc: 'JWT authentication with role-based access control' },
  ]

  return (
    <div>
      <section className="hero">
        <h1>Vehicle Parking System</h1>
        <p>
          A complete parking management solution — vehicle tracking, slot allocation,
          fee calculation, payments, reservations, and administrative monitoring.
        </p>
        <div className="hero-actions">
          {isAuthenticated ? (
            <Link to={dashboardPath} className="btn btn-primary">Go to Dashboard</Link>
          ) : (
            <>
              <Link to="/register" className="btn btn-primary">Get Started</Link>
              <Link to="/login" className="btn btn-outline" style={{ color: 'var(--gray-700)', borderColor: 'var(--gray-300)' }}>Login</Link>
            </>
          )}
        </div>
      </section>

      <div className="features-grid">
        {features.map(f => (
          <div key={f.title} className="card feature-card">
            <div className="icon">{f.icon}</div>
            <h3>{f.title}</h3>
            <p>{f.desc}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
