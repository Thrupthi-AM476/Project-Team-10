import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Navbar() {
  const { isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    setMenuOpen(false)
    navigate('/')
  }

  const publicLinks = [
    { to: '/parking', label: 'Parking Availability' },
  ]

  const customerLinks = [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/vehicles', label: 'Vehicles' },
    { to: '/parking', label: 'Parking' },
    { to: '/reservations', label: 'Reservations' },
    { to: '/profile', label: 'Profile' },
  ]

  const attendantLinks = [
    { to: '/staff', label: 'Staff Dashboard' },
    { to: '/parking', label: 'Parking' },
    { to: '/vehicles', label: 'Vehicles' },
    { to: '/reservations', label: 'Reservations' },
    { to: '/profile', label: 'Profile' },
  ]

  // Maintenance technicians retain general access but not slot-management UI.
  const maintenanceLinks = [
    { to: '/parking', label: 'Parking' },
    { to: '/vehicles', label: 'Vehicles' },
    { to: '/reservations', label: 'Reservations' },
    { to: '/profile', label: 'Profile' },
  ]

  const adminLinks = [
    { to: '/admin', label: 'Admin' },
    { to: '/staff', label: 'Staff Dashboard' },
    { to: '/parking', label: 'Parking' },
    { to: '/vehicles', label: 'Vehicles' },
    { to: '/reservations', label: 'Reservations' },
    { to: '/profile', label: 'Profile' },
  ]

  let links = publicLinks
  if (user?.role === 'ADMINISTRATOR') links = adminLinks
  else if (user?.role === 'PARKING_ATTENDANT') links = attendantLinks
  else if (user?.role === 'MAINTENANCE_TECHNICIAN') links = maintenanceLinks
  else if (isAuthenticated) links = customerLinks

  const roleLabel = user?.role?.replace(/_/g, ' ').toLowerCase()

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <NavLink to="/">
          <span className="logo">🅿</span> VPS
        </NavLink>
      </div>

      <button
        className="navbar-toggle"
        onClick={() => setMenuOpen(!menuOpen)}
        aria-label="Toggle navigation"
        aria-expanded={menuOpen}
      >
        ☰
      </button>

      <div className={`navbar-links ${menuOpen ? 'open' : ''}`}>
        {links.map(link => (
          <NavLink
            key={link.to}
            to={link.to}
            onClick={() => setMenuOpen(false)}
            className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}
          >
            {link.label}
          </NavLink>
        ))}

        {isAuthenticated ? (
          <div className="navbar-user">
            <span className="navbar-userinfo">
              {user?.fullName} · <em>{roleLabel}</em>
            </span>
            <button className="btn btn-sm btn-outline" onClick={handleLogout}>
              Logout
            </button>
          </div>
        ) : (
          <div className="navbar-user">
            <NavLink to="/login" className="btn btn-sm btn-outline" onClick={() => setMenuOpen(false)}>
              Login
            </NavLink>
            <NavLink to="/register" className="btn btn-sm btn-primary" onClick={() => setMenuOpen(false)}>
              Register
            </NavLink>
          </div>
        )}
      </div>
    </nav>
  )
}
