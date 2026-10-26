/**
 * Reusable UI primitives for the Vehicle Parking System frontend.
 */
import { useState } from 'react'

export function LoadingSpinner({ label = 'Loading…' }) {
  return (
    <div className="spinner-wrap" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true"></div>
      <span>{label}</span>
    </div>
  )
}

export function EmptyState({ title, message, action }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      <p>{message}</p>
      {action}
    </div>
  )
}

export function ErrorMessage({ message, onDismiss }) {
  if (!message) return null
  return (
    <div className="alert alert-error" role="alert">
      <span>{message}</span>
      {onDismiss && (
        <button className="alert-dismiss" onClick={onDismiss} aria-label="Dismiss">
          ✕
        </button>
      )}
    </div>
  )
}

export function SuccessMessage({ message }) {
  if (!message) return null
  return (
    <div className="alert alert-success" role="status">
      <span>{message}</span>
    </div>
  )
}

export function Badge({ variant = 'default', children }) {
  return <span className={`badge badge-${variant}`}>{children}</span>
}

export function Card({ children, className = '' }) {
  return <div className={`card ${className}`}>{children}</div>
}

export function ConfirmDialog({ open, title, message, onConfirm, onCancel, confirmLabel = 'Confirm' }) {
  if (!open) return null
  return (
    <div className="dialog-overlay" role="dialog" aria-modal="true" aria-label={title}>
      <div className="dialog">
        <h3>{title}</h3>
        <p>{message}</p>
        <div className="dialog-actions">
          <button className="btn btn-secondary" onClick={onCancel}>Cancel</button>
          <button className="btn btn-danger" onClick={onConfirm}>{confirmLabel}</button>
        </div>
      </div>
    </div>
  )
}

/**
 * Reusable form field wrapper with label and error display.
 */
export function Field({ label, error, children, htmlFor }) {
  return (
    <div className="field">
      <label htmlFor={htmlFor}>{label}</label>
      {children}
      {error && <span className="field-error">{error}</span>}
    </div>
  )
}

/**
 * Hook for simple form error state management.
 */
export function useFormErrors() {
  const [errors, setErrors] = useState({})
  const setFieldError = (field, msg) => setErrors(prev => ({ ...prev, [field]: msg }))
  const clearErrors = () => setErrors({})
  return { errors, setFieldError, clearErrors }
}
