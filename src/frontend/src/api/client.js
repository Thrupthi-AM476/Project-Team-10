/**
 * Centralized API client for the Vehicle Parking System backend.
 * Handles JWT auth headers, error normalization, and consistent responses.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

function getToken() {
  return localStorage.getItem('vps_token')
}

export function setToken(token) {
  if (token) localStorage.setItem('vps_token', token)
  else localStorage.removeItem('vps_token')
}

async function request(method, path, body = null, auth = true) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = getToken()
    if (token) headers['Authorization'] = `Bearer ${token}`
  }

  const config = { method, headers }
  if (body) config.body = JSON.stringify(body)

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, config)
  } catch (err) {
    throw { success: false, message: 'Unable to reach the server. Is the backend running?', status: 0 }
  }

  let data
  try {
    data = await response.json()
  } catch {
    data = { success: false, message: 'Unexpected server response' }
  }

  if (!response.ok) {
    const error = {
      success: false,
      message: data.message || 'Request failed',
      status: response.status,
    }
    throw error
  }

  return data
}

// ---- Auth ----
export const authApi = {
  register: (payload) => request('POST', '/auth/register', payload, false),
  login: (payload) => request('POST', '/auth/login', payload, false),
}

// ---- User Profile ----
export const userApi = {
  getProfile: () => request('GET', '/users/me'),
  updateProfile: (payload) => request('PUT', '/users/me', payload),
}

// ---- Vehicles ----
export const vehicleApi = {
  list: () => request('GET', '/vehicles'),
  getById: (id) => request('GET', `/vehicles/${id}`),
  searchByPlate: (plate) => request('GET', `/vehicles/search?plate=${encodeURIComponent(plate)}`),
  create: (payload) => request('POST', '/vehicles', payload),
  update: (id, payload) => request('PUT', `/vehicles/${id}`, payload),
  deactivate: (id) => request('DELETE', `/vehicles/${id}`),
}

// ---- Parking ----
export const parkingApi = {
  getAllSlots: () => request('GET', '/parking/slots', null, false),
  getAvailableSlots: () => request('GET', '/parking/slots/available', null, false),
  createSlot: (payload) => request('POST', '/parking/slots', payload),
  updateSlot: (id, payload) => request('PUT', `/parking/slots/${id}`, payload),
  deactivateSlot: (id) => request('DELETE', `/parking/slots/${id}`),
  processEntry: (payload) => request('POST', '/parking/entry', payload),
  processExit: (payload) => request('POST', '/parking/exit', payload),
  getHistory: () => request('GET', '/parking/history'),
  getVehicleHistory: (vehicleId) => request('GET', `/parking/history/vehicle/${vehicleId}`),
  getActiveSession: () => request('GET', '/parking/active-session'),
}

// ---- Reservations ----
export const reservationApi = {
  list: () => request('GET', '/reservations'),
  create: (payload) => request('POST', '/reservations', payload),
  cancel: (id) => request('DELETE', `/reservations/${id}`),
  expire: () => request('POST', '/reservations/expire'),
}

// ---- Admin ----
export const adminApi = {
  getDashboard: () => request('GET', '/admin/dashboard'),
  getReports: (from, to) => {
    const params = new URLSearchParams()
    if (from) params.append('from', from)
    if (to) params.append('to', to)
    const qs = params.toString()
    return request('GET', `/admin/reports${qs ? '?' + qs : ''}`)
  },
  getRates: () => request('GET', '/admin/rates'),
  createRate: (payload) => request('POST', '/admin/rates', payload),
  updateRate: (id, payload) => request('PUT', `/admin/rates/${id}`, payload),
}
