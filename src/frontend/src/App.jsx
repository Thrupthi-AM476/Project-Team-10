import { Routes, Route } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute, RoleRoute } from './components/ProtectedRoute'
import { Landing } from './pages/Landing'
import { Register } from './pages/Register'
import { Login } from './pages/Login'
import { CustomerDashboard } from './pages/CustomerDashboard'
import { Vehicles } from './pages/Vehicles'
import { ParkingAvailability } from './pages/ParkingAvailability'
import { ParkingSession } from './pages/ParkingSession'
import { ParkingExit } from './pages/ParkingExit'
import { Reservations } from './pages/Reservations'
import { Profile } from './pages/Profile'
import { StaffDashboard } from './pages/StaffDashboard'
import { AdminDashboard } from './pages/AdminDashboard'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Customer + staff authenticated routes */}
        <Route path="/dashboard" element={
          <ProtectedRoute><CustomerDashboard /></ProtectedRoute>
        } />
        <Route path="/vehicles" element={
          <ProtectedRoute><Vehicles /></ProtectedRoute>
        } />
        <Route path="/parking" element={
          <ProtectedRoute><ParkingAvailability /></ProtectedRoute>
        } />
        <Route path="/parking/session" element={
          <ProtectedRoute><ParkingSession /></ProtectedRoute>
        } />
        <Route path="/parking/exit" element={
          <ProtectedRoute><ParkingExit /></ProtectedRoute>
        } />
        <Route path="/reservations" element={
          <ProtectedRoute><Reservations /></ProtectedRoute>
        } />
        <Route path="/profile" element={
          <ProtectedRoute><Profile /></ProtectedRoute>
        } />

        {/* Staff-only routes */}
        <Route path="/staff" element={
          <ProtectedRoute roles={['PARKING_ATTENDANT', 'ADMINISTRATOR', 'MAINTENANCE_TECHNICIAN']}>
            <StaffDashboard />
          </ProtectedRoute>
        } />

        {/* Admin-only routes */}
        <Route path="/admin" element={
          <ProtectedRoute roles={['ADMINISTRATOR']}>
            <AdminDashboard />
          </ProtectedRoute>
        } />

        <Route path="*" element={<Landing />} />
      </Route>
    </Routes>
  )
}
