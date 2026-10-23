import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { vehicleApi, parkingApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { Card, LoadingSpinner, EmptyState, ErrorMessage, SuccessMessage, Field } from '../components/ui'

export function ParkingSession() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [vehicles, setVehicles] = useState([])
  const [activeSession, setActiveSession] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Entry form
  const [plate, setPlate] = useState('')
  const [slotType, setSlotType] = useState('')
  const [processing, setProcessing] = useState(false)

  useEffect(() => {
    async function load() {
      try {
        const [vRes, sRes] = await Promise.all([
          vehicleApi.list(),
          parkingApi.getActiveSession(),
        ])
        setVehicles((vRes.data || []).filter(v => v.isActive))
        const sessions = sRes.data || []
        setActiveSession(sessions.length > 0 ? sessions[0] : null)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const handleEntry = async (e) => {
    e.preventDefault()
    setError('')
    setProcessing(true)
    try {
      const res = await parkingApi.processEntry({
        licensePlate: plate,
        slotType: slotType || undefined,
        captureMethod: 'MANUAL',
      })
      setSuccess(`Entry recorded! Ticket: ${res.data.ticketId}, Slot: ${res.data.slotNumber}`)
      setActiveSession(res.data)
      setPlate('')
    } catch (err) {
      setError(err.message)
    } finally {
      setProcessing(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading…" />

  return (
    <div>
      <div className="page-header">
        <h1>Parking Entry</h1>
        <p>Allocate a parking slot and generate an entry ticket</p>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {activeSession ? (
        <Card>
          <h3 className="mb-1">Active Parking Session</h3>
          <div className="receipt">
            <div className="receipt-row"><span>Ticket ID</span><strong>{activeSession.ticketId}</strong></div>
            <div className="receipt-row"><span>License Plate</span><strong>{activeSession.licensePlate}</strong></div>
            <div className="receipt-row"><span>Slot</span><strong>{activeSession.slotNumber} ({activeSession.slotType})</strong></div>
            <div className="receipt-row"><span>Entry Time</span><strong>{new Date(activeSession.entryTime).toLocaleString()}</strong></div>
          </div>
          <button className="btn btn-primary" onClick={() => navigate('/parking/exit')}>
            Process Exit & Pay →
          </button>
        </Card>
      ) : (
        <Card>
          <h3 className="mb-1">Park a Vehicle</h3>
          {vehicles.length === 0 ? (
            <EmptyState title="No active vehicles" message="Register a vehicle first."
              action={<Link to="/vehicles" className="btn btn-primary btn-sm">Add Vehicle</Link>} />
          ) : (
            <form onSubmit={handleEntry}>
              <Field label="Select Vehicle" htmlFor="vehicle-select">
                <select id="vehicle-select" value={plate} onChange={e => setPlate(e.target.value)} required>
                  <option value="">— Choose a vehicle —</option>
                  {vehicles.map(v => (
                    <option key={v.id} value={v.licensePlate}>
                      {v.licensePlate} — {v.make} {v.model}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="Preferred Slot Type (optional)" htmlFor="slot-type">
                <select id="slot-type" value={slotType} onChange={e => setSlotType(e.target.value)}>
                  <option value="">Any available</option>
                  <option value="REGULAR">Regular</option>
                  <option value="DISABLED">Disabled</option>
                  <option value="EV">EV</option>
                </select>
              </Field>
              <button type="submit" className="btn btn-primary" disabled={processing}>
                {processing ? 'Allocating…' : 'Allocate Slot & Generate Ticket'}
              </button>
            </form>
          )}
        </Card>
      )}
    </div>
  )
}
