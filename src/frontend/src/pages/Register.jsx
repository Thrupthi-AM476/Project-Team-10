import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ErrorMessage, Field, SuccessMessage } from '../components/ui'

export function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    fullName: '', email: '', password: '', phone: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (field) => (e) => setForm(prev => ({ ...prev, [field]: e.target.value }))

  const validatePassword = (pw) => {
    if (pw.length < 8) return 'Password must be at least 8 characters'
    if (!/[a-z]/.test(pw)) return 'Password must contain a lowercase letter'
    if (!/[A-Z]/.test(pw)) return 'Password must contain an uppercase letter'
    if (!/\d/.test(pw)) return 'Password must contain a number'
    if (!/[@$!%*?&]/.test(pw)) return 'Password must contain a special character (@$!%*?&)'
    return null
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    const pwError = validatePassword(form.password)
    if (pwError) { setError(pwError); return }

    setLoading(true)
    try {
      await register(form)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <h1>Create Account</h1>
      <p>Register as a customer to start parking</p>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}

      <form onSubmit={handleSubmit} className="card">
        <Field label="Full Name" htmlFor="fullName">
          <input id="fullName" type="text" value={form.fullName}
            onChange={update('fullName')} required placeholder="John Doe" />
        </Field>

        <Field label="Email" htmlFor="email">
          <input id="email" type="email" value={form.email}
            onChange={update('email')} required placeholder="you@example.com" />
        </Field>

        <Field label="Phone (optional)" htmlFor="phone">
          <input id="phone" type="tel" value={form.phone}
            onChange={update('phone')} placeholder="9876543210" />
        </Field>

        <Field label="Password" htmlFor="password"
          error="Min 8 chars with upper, lower, number, and special char (@$!%*?&)">
          <input id="password" type="password" value={form.password}
            onChange={update('password')} required placeholder="••••••••" />
        </Field>

        <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
          {loading ? 'Creating account…' : 'Register'}
        </button>
      </form>

      <p className="text-center mt-1 text-sm text-gray">
        Already have an account? <Link to="/login">Login here</Link>
      </p>
    </div>
  )
}
