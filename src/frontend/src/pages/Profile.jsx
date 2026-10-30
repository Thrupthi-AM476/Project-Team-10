import { useEffect, useState } from 'react'
import { userApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { Card, LoadingSpinner, ErrorMessage, SuccessMessage, Badge } from '../components/ui'

export function Profile() {
  const { user, updateUser } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [fetchError, setFetchError] = useState('')
  const [formError, setFormError] = useState('')
  const [success, setSuccess] = useState('')
  const [saving, setSaving] = useState(false)

  const [form, setForm] = useState({ fullName: '', phone: '' })
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    async function load() {
      try {
        const res = await userApi.getProfile()
        setProfile(res.data)
        setForm({ fullName: res.data.fullName || '', phone: res.data.phone || '' })
      } catch (err) {
        setFetchError(err.message || 'Could not load profile.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }))
    setDirty(true)
    setSuccess('')
    setFormError('')
  }

  const handleSubmit = async e => {
    e.preventDefault()
    setFormError('')
    if (!form.fullName.trim()) {
      setFormError('Full name is required.')
      return
    }
    setSaving(true)
    try {
      const res = await userApi.updateProfile(form)
      setProfile(res.data)
      updateUser({ fullName: res.data.fullName })
      setSuccess('Profile updated successfully.')
      setDirty(false)
    } catch (err) {
      setFormError(err.message || 'Could not save changes.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading profile…" />

  const joinDate = profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'long', year: 'numeric',
  }) : '—'

  return (
    <main>
      <div className="page-header">
        <h1>My Profile</h1>
        <p>View and update your account details</p>
      </div>

      {fetchError && <ErrorMessage message={fetchError} />}
      {success    && <SuccessMessage message={success} />}

      <div className="grid-2">
        {/* Account info */}
        <Card>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.25rem' }}>
            <div style={{
              width: 52, height: 52,
              borderRadius: '50%',
              background: 'var(--primary)',
              color: '#fff',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: '1.35rem', fontWeight: 700, flexShrink: 0,
            }}>
              {profile?.fullName?.[0]?.toUpperCase() || '?'}
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{profile?.fullName}</div>
              <div className="text-sm text-gray">{profile?.email}</div>
            </div>
          </div>

          <p className="section-title">Account Details</p>
          <div className="info-list">
            <div className="info-row">
              <span className="info-label">Full name</span>
              <span className="info-value">{profile?.fullName}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Email</span>
              <span className="info-value" style={{ fontSize: '.85rem' }}>{profile?.email}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Phone</span>
              <span className="info-value">{profile?.phone || <span className="text-muted">Not set</span>}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Role</span>
              <Badge variant="primary">
                {profile?.role?.replace(/_/g, ' ').toLowerCase()}
              </Badge>
            </div>
            <div className="info-row">
              <span className="info-label">Account status</span>
              <Badge variant={profile?.isActive ? 'success' : 'danger'}>
                {profile?.isActive ? 'Active' : 'Inactive'}
              </Badge>
            </div>
            <div className="info-row">
              <span className="info-label">Member since</span>
              <span className="info-value">{joinDate}</span>
            </div>
          </div>
        </Card>

        {/* Edit form */}
        <Card>
          <p className="section-title">Edit Profile</p>

          {formError && <ErrorMessage message={formError} onDismiss={() => setFormError('')} />}

          <form onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label htmlFor="p-name">
                Full name <span aria-hidden="true" style={{ color: 'var(--danger)' }}>*</span>
              </label>
              <input
                id="p-name"
                type="text"
                value={form.fullName}
                onChange={e => handleChange('fullName', e.target.value)}
                required
                autoComplete="name"
                aria-required="true"
              />
            </div>

            <div className="field">
              <label htmlFor="p-phone">
                Phone <span className="text-muted text-xs">(optional)</span>
              </label>
              <input
                id="p-phone"
                type="tel"
                value={form.phone}
                onChange={e => handleChange('phone', e.target.value)}
                autoComplete="tel"
                placeholder="9876543210"
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={saving || !dirty}
              aria-busy={saving}
            >
              {saving ? (
                <>
                  <span className="spinner" style={{ width: 14, height: 14, borderWidth: 2 }} aria-hidden="true" />
                  Saving…
                </>
              ) : 'Save Changes'}
            </button>
          </form>

          <div className="notice mt-2">
            <span>ℹ️</span>
            <span>
              Email, role, and account status can only be changed by an administrator.
              Password changes are not supported in the current release.
            </span>
          </div>
        </Card>
      </div>
    </main>
  )
}
