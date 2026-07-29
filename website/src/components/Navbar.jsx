import { useState, useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import ThemeToggle from './ThemeToggle'

const links = [
  { to: '/', label: 'Home' },
  { to: '/reading', label: 'Reading' },
  { to: '/docs', label: 'Docs' },
  { to: '/changelog', label: 'Changelog' },
]

export default function Navbar() {
  const { pathname } = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    if (!menuOpen) return
    const handler = (e) => { if (e.key === 'Escape') setMenuOpen(false) }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [menuOpen])

  return (
    <nav role="navigation" aria-label="Main navigation" style={{
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '12px 20px', maxWidth: 'var(--max-width)', margin: '0 auto',
      position: 'sticky', top: 0, zIndex: 100, background: 'color-mix(in srgb, var(--md-surface) 85%, transparent)', backdropFilter: 'blur(8px)', WebkitBackdropFilter: 'blur(8px)',
    }}>
      <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 8, textDecoration: 'none' }}>
        <img src="/assets/logo.png" alt="QuickDash logo" width="32" height="32" className="pixel-art" />
        <span style={{ fontFamily: 'var(--pixel-font)', fontSize: 12, color: 'var(--md-primary)' }}>
          QUICKDASH
        </span>
      </Link>

      <button
        className="nav-mobile-toggle"
        onClick={() => setMenuOpen(o => !o)}
        aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
        aria-expanded={menuOpen}
        style={{
          display: 'none', background: 'none', border: 'none', cursor: 'pointer',
          fontFamily: 'var(--pixel-font)', fontSize: 16, color: 'var(--md-primary)',
        }}
      >
        {menuOpen ? '✕' : '☰'}
      </button>

      <div className="nav-links" style={{
        display: 'flex', alignItems: 'center', gap: 4,
      }}>
        {links.map(l => (
          <Link
            key={l.to}
            to={l.to}
            aria-current={pathname === l.to ? 'page' : undefined}
            className="btn btn-sm btn-outline"
            style={{
              opacity: pathname === l.to ? 1 : 0.7,
              outline: pathname === l.to ? '2px solid var(--md-primary)' : undefined,
              outlineOffset: -2,
              background: pathname === l.to ? 'var(--md-primary-container)' : undefined,
            }}
          >
            {l.label}
          </Link>
        ))}
        <a
          href="https://github.com/Balajitechlabs/quickdash/issues"
          target="_blank"
          rel="noopener noreferrer"
          className="btn btn-sm btn-outline"
          aria-label="Report issues on GitHub"
          title="Report Issue"
        >
          🐛
        </a>
        <ThemeToggle />
      </div>

      <style>{`
        @media (max-width: 640px) {
          .nav-mobile-toggle { display: block !important; }
          .nav-links {
            display: ${menuOpen ? 'flex' : 'none'} !important;
            flex-direction: column; position: absolute; top: 100%; left: 0; right: 0;
            background: var(--md-surface); border: 1px solid var(--md-outline);
            border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.12);
            padding: 12px; gap: 8px; z-index: 200;
          }
          .nav-links .btn { width: 100%; justify-content: center; }
        }
      `}</style>
    </nav>
  )
}
