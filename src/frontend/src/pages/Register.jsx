import { useState, useMemo } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ErrorMessage } from '../components/ui'

// Password rule definitions — mirroring the backend's pattern requirement
const PW_RULES = [
  { id: 'len',     label: 'At least 8 characters',                test: pw => pw.length >= 8 },
  { id: 'upper',   label: 'One uppercase letter (A–Z)',           test: pw => /[A-Z]/.test(pw) },
  { id: 'lower',   label: 'One lowercase letter (a–z)',           test: pw => /[a-z]/.test(pw) },
  { id: 'digit',   label: 'One number (0–9)',                     test: pw => /\d/.test(pw) },
  { id: 'special', label: 'One special character (@$!%*?&)',      test: pw => /[@$!%*?&]/.test(pw) },
]

function passwordStrength(pw) {
  const passed = PW_RULES.filter(r => r.test(pw)).length
  if (passed <= 2) return 'weak'
  if (passed <= 4) return 'medium'
  return 'strong'
}

export function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirm: '', phone: '' })
  const [touched, setTouched] = useState({})
  const [showPw, setShowPw] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = field => e =>
    setForm(prev => ({ ...prev, [field]: e.target.value }))
  const blur  = field => () =>
    setTouched(prev => ({ ...prev, [field]: true }))

  // Live rule checks
  const rulesMet = useMemo(
    () => PW_RULES.reduce((acc, r) => { acc[r.id] = r.test(form.password); return acc }, {}),
    [form.password]
  )
  const allRulesMet = Object.values(rulesMet).every(Boolean)
  const strength = form.password ? passwordStrength(form.password) : null

  // Field-level errors (only shown after blur)
  const emailError     = touched.email    && !form.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/) ? 'Enter a valid email address' : ''
  const nameError      = touched.fullName && form.fullName.trim().length < 2 ? 'Name must be at least 2 characters' : ''
  const confirmError   = touched.confirm  && form.confirm && form.confirm !== form.password ? 'Passwords do not match' : ''

  const canSubmit = form.fullName.trim() && form.email && allRulesMet && form.confirm === form.password && !loading

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')

    // Force all fields touched so errors show
    setTouched({ fullName: true, email: true, password: true, confirm: true })

    if (!allRulesMet) { setError('Your password does not meet the requirements below.'); return }
    if (form.confirm !== form.password) { setError('Passwords do not match.'); return }

    setLoading(true)
    try {
      await register({ fullName: form.fullName.trim(), email: form.email, password: form.password, phone: form.phone })
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(
        err.status === 400 && err.message?.toLowerCase().includes('email')
          ? 'This email is already registered. Try logging in instead.'
          : err.message || 'Registration failed. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <a href="#register-form" className="skip-link">Skip to form</a>

      <div className="auth-logo">
        <div className="auth-logo-icon">🅿</div>
        <h1>Create account</h1>
        <p className="auth-subtitle">Register as a customer to start parking</p>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}

      <form id="register-form" onSubmit={handleSubmit} className="card" noValidate>
        {/* Full Name */}
        <div className="field">
          <label htmlFor="fullName">Full name <span aria-hidden="true" style={{ color: 'var(--danger)' }}>*</span></label>
          <input
            id="fullName" type="text" value={form.fullName}
            onChange={update('fullName')} onBlur={blur('fullName')}
            required autoComplete="name" placeholder="John Doe"
            aria-required="true"
            className={nameError ? 'input-error' : ''}
            aria-describedby={nameError ? 'err-name' : undefined}
          />
          {nameError && <span id="err-name" className="field-error" role="alert">{nameError}</span>}
        </div>

        {/* Email */}
        <div className="field">
          <label htmlFor="reg-email">Email address <span aria-hidden="true" style={{ color: 'var(--danger)' }}>*</span></label>
          <input
            id="reg-email" type="email" value={form.email}
            onChange={update('email')} onBlur={blur('email')}
            required autoComplete="email" placeholder="you@example.com"
            aria-required="true"
            className={emailError ? 'input-error' : ''}
            aria-describedby={emailError ? 'err-email' : undefined}
          />
          {emailError && <span id="err-email" className="field-error" role="alert">{emailError}</span>}
        </div>

        {/* Phone */}
        <div className="field">
          <label htmlFor="phone">Phone <span className="text-muted text-xs">(optional)</span></label>
          <input
            id="phone" type="tel" value={form.phone}
            onChange={update('phone')}
            autoComplete="tel" placeholder="9876543210"
          />
        </div>

        {/* Password */}
        <div className="field">
          <label htmlFor="reg-password">Password <span aria-hidden="true" style={{ color: 'var(--danger)' }}>*</span></label>
          <div style={{ position: 'relative' }}>
            <input
              id="reg-password"
              type={showPw ? 'text' : 'password'}
              value={form.password}
              onChange={update('password')}
              onBlur={blur('password')}
              required autoComplete="new-password" placeholder="••••••••"
              aria-required="true" aria-describedby="pw-rules"
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

          {/* Strength bar */}
          {form.password && (
            <div className="pw-strength" aria-live="polite">
              <div className="pw-strength-bar" aria-hidden="true">
                <div className={`pw-strength-fill ${strength}`} />
              </div>
              <span className={`pw-strength-label ${strength}`}>
                {strength === 'weak' ? 'Weak' : strength === 'medium' ? 'Medium' : 'Strong'}
              </span>
            </div>
          )}

          {/* Rule checklist */}
          <ul id="pw-rules" className="pw-rules" aria-label="Password requirements">
            {PW_RULES.map(r => (
              <li key={r.id} className={`pw-rule ${rulesMet[r.id] ? 'met' : ''}`}>
                {r.label}
              </li>
            ))}
          </ul>
        </div>

        {/* Confirm password */}
        <div className="field">
          <label htmlFor="confirm">Confirm password <span aria-hidden="true" style={{ color: 'var(--danger)' }}>*</span></label>
          <input
            id="confirm" type="password" value={form.confirm}
            onChange={update('confirm')} onBlur={blur('confirm')}
            required autoComplete="new-password" placeholder="••••••••"
            aria-required="true"
            className={confirmError ? 'input-error' : form.confirm && form.confirm === form.password ? '' : ''}
            aria-describedby={confirmError ? 'err-confirm' : undefined}
          />
          {confirmError && <span id="err-confirm" className="field-error" role="alert">{confirmError}</span>}
          {!confirmError && form.confirm && form.confirm === form.password && (
            <span className="field-success">✓ Passwords match</span>
          )}
        </div>

        <button
          type="submit"
          className="btn btn-primary btn-block"
          disabled={!canSubmit}
          aria-busy={loading}
        >
          {loading ? (
            <>
              <span className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} aria-hidden="true" />
              Creating account…
            </>
          ) : 'Create account'}
        </button>
      </form>

      <p className="text-center mt-1 text-sm text-gray">
        Already have an account?{' '}
        <Link to="/login" className="font-medium">Sign in</Link>
      </p>
    </div>
  )
}
