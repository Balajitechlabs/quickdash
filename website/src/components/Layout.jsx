import { useEffect, useState, Component } from 'react'
import { useLocation } from 'react-router-dom'
import Navbar from './Navbar'
import Footer from './Footer'
import UpdatePopup from './UpdatePopup'

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])
  return null
}

function BackToTop() {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const onScroll = () => setVisible(window.scrollY > 400)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  if (!visible) return null

  return (
    <button
      onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
      className="back-to-top"
      aria-label="Scroll to top"
      style={{
        position: 'fixed', bottom: 24, left: 24, zIndex: 999,
        width: 44, height: 44, borderRadius: '50%',
        background: 'var(--md-primary)', color: 'var(--md-on-primary)',
        border: 'none', cursor: 'pointer', fontSize: 18,
        boxShadow: '0 2px 12px rgba(0,0,0,0.2)',
        animation: 'fadeIn 0.2s ease',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}
    >
      ↑
    </button>
  )
}

function ErrorFallback() {
  return (
    <div className="container" style={{ textAlign: 'center', paddingTop: 80, paddingBottom: 80 }}>
      <h1 style={{ fontFamily: 'var(--pixel-font)', fontSize: 24, color: 'var(--md-error)' }}>OOPS!</h1>
      <p style={{ margin: '16px 0', color: 'var(--md-on-surface-variant)' }}>
        Something went wrong. Please refresh the page.
      </p>
      <button className="btn" onClick={() => window.location.reload()}>
        Refresh Page
      </button>
    </div>
  )
}

export default function Layout({ children }) {
  return (
    <>
      <ScrollToTop />
      <a href="#main-content" className="skip-link">Skip to main content</a>
      <Navbar />
      <main id="main-content" role="main">
        <ErrorBoundary fallback={<ErrorFallback />}>
          {children}
        </ErrorBoundary>
      </main>
      <Footer />
      <BackToTop />
      <UpdatePopup />
    </>
  )
}

class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false }
  }
  static getDerivedStateFromError() {
    return { hasError: true }
  }
  render() {
    if (this.state.hasError) return this.props.fallback
    return this.props.children
  }
}
