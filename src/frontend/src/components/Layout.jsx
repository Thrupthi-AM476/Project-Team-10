import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'

export function Layout() {
  return (
    <div className="app-layout">
      <a href="#main-content" className="skip-link">Skip to main content</a>
      <Navbar />
      <div id="main-content" className="app-main" tabIndex={-1}>
        <Outlet />
      </div>
      <footer className="app-footer" role="contentinfo">
        <p>Vehicle Parking System — Team 10 · Software Engineering Project 2026</p>
      </footer>
    </div>
  )
}
