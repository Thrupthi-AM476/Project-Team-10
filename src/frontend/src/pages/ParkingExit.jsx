import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { parkingApi } from '../api/client'
import { Card, LoadingSpinner, ErrorMessage, SuccessMessage, Field } from '../components/ui'

export function ParkingExit() {
  const [activeSession, setActiveSession] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [ticketId, setTicketId] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [processing, setProcessing] = useState(false)
  const [receipt, setReceipt] = useState(null)

  useEffect(() => {
    async function load() {
      try {
        const res = await parkingApi.getActiveSession()
        const sessions = res.data || []
        if (sessions.length > 0) {
          setActiveSession(sessions[0])
          setTicketId(sessions[0].ticketId)
        }
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const handleExit = async (e) => {
    e.preventDefault()
    setError('')
    setProcessing(true)
    setReceipt(null)
    try {
      const res = await parkingApi.processExit({ ticketId, paymentMethod })
      setReceipt(res.data)
      setSuccess('Exit processed and payment recorded (simulated).')
      setActiveSession(null)
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
        <h1>Vehicle Exit & Payment</h1>
        <p>Record exit time, calculate fee, and process payment</p>
      </div>

      {error && <ErrorMessage message={error} onDismiss={() => setError('')} />}
      {success && <SuccessMessage message={success} />}

      {receipt ? (
        <Card>
          <div className="receipt">
            <h3>🧾 Parking Receipt</h3>
            <div className="receipt-row"><span>Receipt ID</span><strong>{receipt.receiptId}</strong></div>
            <div className="receipt-row"><span>Ticket ID</span><strong>{receipt.ticketId}</strong></div>
            <div className="receipt-row"><span>License Plate</span><strong>{receipt.licensePlate}</strong></div>
            <div className="receipt-row"><span>Slot</span><strong>{receipt.slotNumber}</strong></div>
            <div className="receipt-row"><span>Entry Time</span><span>{new Date(receipt.entryTime).toLocaleString()}</span></div>
            <div className="receipt-row"><span>Exit Time</span><span>{new Date(receipt.exitTime).toLocaleString()}</span></div>
            <div className="receipt-row"><span>Duration</span><strong>{receipt.durationMinutes} minutes</strong></div>
            <div className="receipt-row"><span>Payment Method</span><span>{receipt.paymentMethod}</span></div>
            <div className="receipt-row"><span>Payment Status</span><strong>{receipt.paymentStatus}</strong></div>
            <div className="receipt-row receipt-total"><span>Total Fee</span><span>{receipt.currency} {receipt.feeAmount}</span></div>
          </div>
          <p className="text-sm text-gray text-center mt-1">
            Payment is simulated for this academic project. No real money was processed.
          </p>
          <div className="text-center mt-1">
            <Link to="/parking" className="btn btn-secondary">Back to Parking</Link>
          </div>
        </Card>
      ) : activeSession ? (
        <Card>
          <h3 className="mb-1">Process Exit</h3>
          {activeSession && (
            <div className="mb-1 text-sm text-gray">
              Active session: <strong>{activeSession.ticketId}</strong> —
              Plate {activeSession.licensePlate} — Slot {activeSession.slotNumber}
            </div>
          )}
          <form onSubmit={handleExit}>
            <Field label="Ticket ID" htmlFor="ticketId">
              <input id="ticketId" type="text" value={ticketId}
                onChange={e => setTicketId(e.target.value)} required
                placeholder="TKT-20261022-00001" />
            </Field>
            <Field label="Payment Method" htmlFor="paymentMethod">
              <select id="paymentMethod" value={paymentMethod}
                onChange={e => setPaymentMethod(e.target.value)}>
                <option value="CASH">Cash</option>
                <option value="CARD">Card</option>
                <option value="DIGITAL_WALLET">Digital Wallet</option>
              </select>
            </Field>
            <button type="submit" className="btn btn-primary" disabled={processing}>
              {processing ? 'Processing…' : 'Process Exit & Pay'}
            </button>
          </form>
        </Card>
      ) : (
        <Card>
          <h3 className="mb-1">Process Exit</h3>
          <p className="text-sm text-gray mb-1">
            No active parking session found. You can enter a ticket ID manually to process exit and payment.
          </p>
          <form onSubmit={handleExit}>
            <Field label="Ticket ID" htmlFor="ticketId">
              <input id="ticketId" type="text" value={ticketId}
                onChange={e => setTicketId(e.target.value)} required
                placeholder="TKT-20261022-00001" />
            </Field>
            <Field label="Payment Method" htmlFor="paymentMethod">
              <select id="paymentMethod" value={paymentMethod}
                onChange={e => setPaymentMethod(e.target.value)}>
                <option value="CASH">Cash</option>
                <option value="CARD">Card</option>
                <option value="DIGITAL_WALLET">Digital Wallet</option>
              </select>
            </Field>
            <button type="submit" className="btn btn-primary" disabled={processing}>
              {processing ? 'Processing…' : 'Process Exit & Pay'}
            </button>
          </form>
        </Card>
      )}
    </div>
  )
}
