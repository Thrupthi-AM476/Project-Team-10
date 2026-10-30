import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ErrorMessage } from '../components/ui'

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await login(email, password)
      const role = res.data.role
      const dest =
        from ||
        (role === 'ADMINISTRATOR'
          ? '/admin'
          : role === 'PARKING_ATTENDANT' || role === 'MAINTENANCE_TECHNICIAN'
          ? '/staff'
          : '/dashboard')
      navigate(dest, { replace: true })
    } catch (err) {
      setError(
        err.status === 403
          ? 'Your account is locked or inactive. Contact an administrator.'
          : err.message || 'Invalid email or password.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <a href="#main-form" className="skip-link">Skip to form</a>

      <div className="auth-logo">
        <div className="auth-logo-icon">🅿</div>
        <h1>Welcome back</h1>
        <p className="auth-subtitle">Sign in to your VPS account</p>
      </div>

      {error && (
        <ErrorMessage message={error} onDismiss={() => setError('')} />
      )}

      <form id="main-form" onSubmit={handleSubmit} className="card" noValidate>
        <div className="field">
          <label htmlFor="email">Email address</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
            autoComplete="email"
            placeholder="you@example.com"
            aria-required="true"
            aria-describedby={error ? 'login-error' : undefined}
          />
        </div>

        <div className="field">
          <div className="flex-between mb-05">
            <label htmlFor="password">Password</label>
          </div>
          <div style={{ position: 'relative' }}>
            <input
              id="password"
              type={showPw ? 'text' : 'password'}
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              placeholder="••••••••"
              aria-required="true"
              style={{ paddingRight: '3rem' }}
            />
            <button
              type="button"
              onClick={() => setShowPw(v => !v)}
              aria-label={showPw ? 'Hide password' : 'Show password'}
              style={{
                position: 'absolute', right: '.75rem', top: '50%',
                transform: 'translateY(-50%)',
                background: 'none', border: 'none', cursor: 'pointer',
                color: 'var(--gray-400)', fontSize: '.9rem', padding: '.2rem',
              }}
            >
              {showPw ? '🙈' : '👁'}
            </button>
          </div>
        </div>

        <button
          type="submit"
          className="btn btn-primary btn-block"
          disabled={loading}
          aria-busy={loading}
        >
          {loading ? (
            <>
              <span className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} aria-hidden="true" />
              Signing in…
            </>
          ) : 'Sign in'}
        </button>
      </form>

      <p className="text-center mt-1 text-sm text-gray">
        Don't have an account?{' '}
        <Link to="/register" className="font-medium">Create one</Link>
      </p>
    </div>
  )
}
