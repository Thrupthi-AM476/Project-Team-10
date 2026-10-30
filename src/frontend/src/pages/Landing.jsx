import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Landing() {
  const { isAuthenticated, user } = useAuth()

  const dashboardPath =
    user?.role === 'ADMINISTRATOR'
      ? '/admin'
      : user?.role === 'PARKING_ATTENDANT' || user?.role === 'MAINTENANCE_TECHNICIAN'
      ? '/staff'
      : '/dashboard'

  const features = [
    { icon: '🚗', title: 'Vehicle Management',     desc: 'Register and manage vehicles with unique license-plate tracking and ownership protection.' },
    { icon: '🅿', title: 'Smart Slot Allocation',  desc: 'Real-time slot availability display and automatic allocation on entry.' },
    { icon: '🎫', title: 'Digital Entry Tickets',  desc: 'Instant ticket generation with QR-ready IDs for every parking session.' },
    { icon: '💳', title: 'Automated Fee Calc',     desc: 'Duration-based fee calculation with grace period and configurable rates.' },
    { icon: '📅', title: 'Advance Reservations',   desc: 'Book parking slots in advance with conflict prevention and auto-expiry.' },
    { icon: '📊', title: 'Admin Dashboard',        desc: 'Monitor occupancy, revenue, user activity, and export date-filtered reports.' },
  ]

  return (
    <div>
      {/* Hero */}
      <section className="hero">
        <span className="hero-badge">Team 10 — SE Project 2026</span>
        <h1>Vehicle Parking System</h1>
        <p>
          A complete end-to-end parking management platform — from vehicle registration and
          slot allocation to fee calculation, payments, reservations, and administrative reporting.
        </p>
        <div className="hero-actions">
          {isAuthenticated ? (
            <Link to={dashboardPath} className="btn btn-primary btn-lg">
              Go to Dashboard →
            </Link>
          ) : (
            <>
              <Link to="/register" className="btn btn-primary btn-lg">
                Get Started Free
              </Link>
              <Link
                to="/login"
                className="btn btn-outline-primary btn-lg"
                style={{ color: 'var(--primary)' }}
              >
                Sign In
              </Link>
            </>
          )}
        </div>
      </section>

      {/* Features grid */}
      <div className="features-grid">
        {features.map(f => (
          <div key={f.title} className="card feature-card">
            <span className="icon">{f.icon}</span>
            <h3>{f.title}</h3>
            <p>{f.desc}</p>
          </div>
        ))}
      </div>

      {/* CTA strip */}
      {!isAuthenticated && (
        <div
          style={{
            textAlign: 'center',
            marginTop: '3rem',
            padding: '2.5rem 1rem',
            background: 'var(--primary)',
            borderRadius: 'var(--radius-lg)',
            color: '#fff',
          }}
        >
          <h2 style={{ marginBottom: '.5rem' }}>Ready to try it?</h2>
          <p style={{ opacity: .85, marginBottom: '1.5rem', fontSize: '.95rem' }}>
            Create a free customer account in seconds.
          </p>
          <Link to="/register" className="btn btn-lg" style={{ background: '#fff', color: 'var(--primary)' }}>
            Create Account
          </Link>
        </div>
      )}
    </div>
  )
}
