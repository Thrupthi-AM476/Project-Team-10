import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Wraps a route that requires authentication.
 * Redirects unauthenticated users to /login, preserving the intended path.
 * Optionally restricts access by role array.
 */
export function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // Preserve the attempted URL so login can redirect back
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (roles && user && !roles.includes(user.role)) {
    // Authenticated but wrong role → send to their default dashboard
    return <Navigate to="/dashboard" replace />
  }

  return children
}

/**
 * Convenience wrapper for strict role-only access (no fallback to customer dashboard).
 */
export function RoleRoute({ children, roles }) {
  const { user } = useAuth()
  if (!user || !roles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />
  }
  return children
}
