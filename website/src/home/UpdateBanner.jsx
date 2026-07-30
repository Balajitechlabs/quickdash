import { useState, useEffect } from 'react'
import FadeInSection from '../components/FadeInSection'
import { trackEvent } from '../utils/analytics'

export default function UpdateBanner() {
  const [latest, setLatest] = useState(null)

  useEffect(() => {
    fetch('/api/v1/update.json')
      .then(r => r.ok ? r.json() : null)
      .then(d => {
        if (d && d.version_name) setLatest(d)
      })
      .catch(() => {})
  }, [])

  if (!latest) return null

  return (
    <FadeInSection as="section" aria-label="Update available">
      <div className="card" style={{
        textAlign: 'center', padding: 24,
        background: 'var(--md-primary-container)',
        border: '1px solid var(--md-primary)',
      }}>
        <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 9, color: 'var(--md-primary)', marginBottom: 8 }}>
          UPDATE AVAILABLE
        </p>
        <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', marginBottom: 16 }}>
          Version {latest.version_name} is now available <small>({latest.version_code})</small>
        </p>
        <a
          href={latest.download_url || 'https://github.com/Balajitechlabs/quickdash/releases/latest'}
          className="btn btn-sm"
          target="_blank"
          rel="noopener noreferrer"
          onClick={() => trackEvent('update_banner_click', latest.version_name)}
        >
          DOWNLOAD
        </a>
      </div>
    </FadeInSection>
  )
}
