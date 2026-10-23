import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ErrorMessage, Field, LoadingSpinner } from '../components/ui'

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await login(email, password)
      const role = res.data.role
      const dest = from || (
        role === 'ADMINISTRATOR' ? '/admin'
        : role === 'PARKING_ATTENDANT' || role === 'MAINTENANCE_TECHNICIAN' ? '/staff'
        : '/dashboard'
      )
      navigate(dest, { replace: true })
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <h1>Welcome Back</h1>
      <p>Log in to your Vehicle Parking System account</p>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}

      <form onSubmit={handleSubmit} className="card">
        <Field label="Email" htmlFor="email">
          <input
            id="email" type="email" value={email}
            onChange={e => setEmail(e.target.value)} required
            autoComplete="email" placeholder="you@example.com"
          />
        </Field>

        <Field label="Password" htmlFor="password">
          <input
            id="password" type="password" value={password}
            onChange={e => setPassword(e.target.value)} required
            autoComplete="current-password" placeholder="••••••••"
          />
        </Field>

        <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
          {loading ? 'Logging in…' : 'Login'}
        </button>
      </form>

      <p className="text-center mt-1 text-sm text-gray">
        Don't have an account? <Link to="/register">Register here</Link>
      </p>
    </div>
  )
}
