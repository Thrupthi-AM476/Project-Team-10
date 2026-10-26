import { useEffect, useState } from 'react'
import { userApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { Card, LoadingSpinner, ErrorMessage, SuccessMessage, Field, Badge } from '../components/ui'

export function Profile() {
  const { user, updateUser } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [form, setForm] = useState({ fullName: '', phone: '' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    async function load() {
      try {
        const res = await userApi.getProfile()
        setProfile(res.data)
        setForm({ fullName: res.data.fullName || '', phone: res.data.phone || '' })
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormError('')
    if (!form.fullName.trim()) { setFormError('Full name is required'); return }
    setSaving(true)
    try {
      const res = await userApi.updateProfile(form)
      setProfile(res.data)
      updateUser({ fullName: res.data.fullName })
      setSuccess('Profile updated successfully')
    } catch (err) {
      setFormError(err.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading profile…" />

  return (
    <div>
      <div className="page-header">
        <h1>My Profile</h1>
        <p>View and update your account information</p>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      <div className="grid-2">
        <Card>
          <h3 className="mb-1">Account Details</h3>
          <div className="receipt-row"><span>Name</span><strong>{profile?.fullName}</strong></div>
          <div className="receipt-row"><span>Email</span><strong>{profile?.email}</strong></div>
          <div className="receipt-row"><span>Phone</span><strong>{profile?.phone || 'Not set'}</strong></div>
          <div className="receipt-row">
            <span>Role</span>
            <Badge variant="primary">{profile?.role?.replace(/_/g, ' ').toLowerCase()}</Badge>
          </div>
          <div className="receipt-row">
            <span>Status</span>
            <Badge variant={profile?.isActive ? 'success' : 'danger'}>
              {profile?.isActive ? 'Active' : 'Inactive'}
            </Badge>
          </div>
          <div className="receipt-row"><span>Member Since</span><span>{profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : '—'}</span></div>
        </Card>

        <Card>
          <h3 className="mb-1">Update Profile</h3>
          <p className="text-sm text-gray mb-1">You can update your full name and phone number.</p>
          {formError && <ErrorMessage message={formError} onDismiss={() => setFormError('')} />}
          <form onSubmit={handleSubmit}>
            <Field label="Full Name" htmlFor="profile-name">
              <input id="profile-name" type="text" value={form.fullName}
                onChange={e => setForm(prev => ({ ...prev, fullName: e.target.value }))} required />
            </Field>
            <Field label="Phone" htmlFor="profile-phone">
              <input id="profile-phone" type="tel" value={form.phone}
                onChange={e => setForm(prev => ({ ...prev, phone: e.target.value }))}
                placeholder="9876543210" />
            </Field>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving…' : 'Save Changes'}
            </button>
          </form>
          <p className="text-sm text-gray mt-1">
            Role, account status, and password are managed by administrators and cannot be changed here.
          </p>
        </Card>
      </div>
    </div>
  )
}
