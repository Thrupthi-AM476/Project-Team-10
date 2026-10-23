import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { authApi, setToken } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => localStorage.getItem('vps_token'))
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(false)

  // Decode the JWT to get user info without an extra network call.
  // The backend AuthResponse includes userId, fullName, email, and role
  // which we persist alongside the token for UI state only.
  // Backend authorization is never bypassed — this is display only.
  const decodeAndStore = useCallback((authData) => {
    setToken(authData.token)
    setTokenState(authData.token)
    setUser({
      id: authData.userId,
      fullName: authData.fullName,
      email: authData.email,
      role: authData.role,
    })
    localStorage.setItem('vps_user', JSON.stringify({
      id: authData.userId,
      fullName: authData.fullName,
      email: authData.email,
      role: authData.role,
    }))
  }, [])

  // Restore cached user info on mount (token validity checked on each API call)
  useEffect(() => {
    const cached = localStorage.getItem('vps_user')
    if (cached) {
      try { setUser(JSON.parse(cached)) } catch { /* ignore */ }
    }
  }, [])

  const login = useCallback(async (email, password) => {
    setLoading(true)
    try {
      const res = await authApi.login({ email, password })
      decodeAndStore(res.data)
      return res
    } finally {
      setLoading(false)
    }
  }, [decodeAndStore])

  const register = useCallback(async (payload) => {
    setLoading(true)
    try {
      const res = await authApi.register(payload)
      decodeAndStore(res.data)
      return res
    } finally {
      setLoading(false)
    }
  }, [decodeAndStore])

  const logout = useCallback(() => {
    setToken(null)
    setTokenState(null)
    setUser(null)
    localStorage.removeItem('vps_user')
  }, [])

  const updateUser = useCallback((updatedUser) => {
    setUser(prev => ({ ...prev, ...updatedUser }))
    localStorage.setItem('vps_user', JSON.stringify({ ...user, ...updatedUser }))
  }, [user])

  const value = {
    token,
    user,
    loading,
    login,
    register,
    logout,
    updateUser,
    isAuthenticated: !!token,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
