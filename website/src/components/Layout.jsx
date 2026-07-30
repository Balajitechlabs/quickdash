import { useEffect, useState, useRef, Component } from 'react'
import { useLocation } from 'react-router-dom'
import Navbar from './Navbar'
import Footer from './Footer'
import UpdatePopup from './UpdatePopup'

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo(0, 0)
    if (typeof window !== 'undefined' && typeof window.gtag === 'function') {
      window.gtag('config', 'G-RBBNT2GRGX', { page_path: pathname })
    }
  }, [pathname])
  return null
}

function SkipLink() {
  return (
    <a href="#main-content" style={{ position: 'absolute', left: '-9999px', top: 0, zIndex: 9999 }}
      onFocus={e => e.target.style.left = '8px'}
      onBlur={e => e.target.style.left = '-9999px'}>
      Skip to content
    </a>
  )
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
  const { pathname } = useLocation()
  const mainRef = useRef(null)

  useEffect(() => {
    if (mainRef.current) {
      mainRef.current.focus()
    }
  }, [pathname])

  return (
    <>
      <ScrollToTop />
      <SkipLink />
      <Navbar />
      <main id="main-content" ref={mainRef} tabIndex={-1} role="main" aria-live="polite">
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
  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo)
    if (typeof gtag !== 'undefined') {
      gtag('event', 'error_boundary', { error: error.message })
    }
  }
  render() {
    if (this.state.hasError) return this.props.fallback
    return this.props.children
  }
}
