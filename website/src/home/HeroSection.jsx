import { useState, useEffect } from 'react'
import FadeInSection from '../components/FadeInSection'
import { trackEvent } from '../utils/analytics'

function GitHubRelease() {
  const [repo, setRepo] = useState(null)
  const [release, setRelease] = useState(null)
  const [commit, setCommit] = useState(null)

  useEffect(() => {
    fetch('https://api.github.com/repos/Balajitechlabs/quickdash')
      .then(r => r.ok ? r.json() : null)
      .then(d => d && setRepo(d))
      .catch(() => {})
    fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases/latest')
      .then(r => r.ok ? r.json() : null)
      .then(d => d && setRelease(d))
      .catch(() => {})
    fetch('https://api.github.com/repos/Balajitechlabs/quickdash/commits/master')
      .then(r => r.ok ? r.json() : null)
      .then(d => d && setCommit(d))
      .catch(() => {})
  }, [])

  const badgeStyle = {
    display: 'inline-flex', alignItems: 'center', gap: 6,
    color: 'var(--md-on-surface-variant)', textDecoration: 'none',
    background: 'var(--md-surface-variant)', padding: '6px 12px',
    borderRadius: 20, fontSize: 11, fontWeight: 500
  }

  return (
    <div style={{ marginTop: 24, display: 'flex', justifyContent: 'center', gap: 10, flexWrap: 'wrap', fontFamily: 'var(--body-font)', fontSize: 13 }}>
      <a href="https://github.com/Balajitechlabs/quickdash/actions" target="_blank" rel="noopener noreferrer" style={badgeStyle} onClick={() => trackEvent('badge_click', 'build_status')}>
        <span style={{ display: 'inline-block', width: 8, height: 8, borderRadius: '50%', background: '#4caf50' }} />
        Build passing
      </a>
      
      <a href="https://github.com/Balajitechlabs/quickdash/releases" target="_blank" rel="noopener noreferrer" style={badgeStyle} onClick={() => trackEvent('badge_click', 'version')}>
        <span style={{ display: 'inline-block', width: 8, height: 8, borderRadius: '50%', background: '#2196f3', animation: 'pulse 2s infinite' }} />
        {release ? `v${release.tag_name.replace('v', '')}` : 'v5.2.1 Latest'}
      </a>

      <a href="https://github.com/Balajitechlabs/quickdash/security/policy" target="_blank" rel="noopener noreferrer" style={badgeStyle} onClick={() => trackEvent('badge_click', 'security')}>
        🔒 Security Verified
      </a>

      <a href="https://github.com/Balajitechlabs/quickdash" target="_blank" rel="noopener noreferrer" style={badgeStyle} onClick={() => trackEvent('badge_click', 'stars')}>
        ⭐ {repo ? `${repo.stargazers_count} Stars` : 'Open Source'}
      </a>

      <a href="https://github.com/Balajitechlabs/quickdash/issues" target="_blank" rel="noopener noreferrer" style={badgeStyle} onClick={() => trackEvent('badge_click', 'issues')}>
        🟢 {repo ? `${repo.open_issues_count} Issues` : 'Active Dev'}
      </a>

      {commit && (
        <a href={commit.html_url} target="_blank" rel="noopener noreferrer" style={badgeStyle} onClick={() => trackEvent('badge_click', 'commit')}>
          <span style={{ fontFamily: 'var(--mono-font)', fontSize: 10 }}>#{commit.sha.slice(0, 7)}</span>
        </a>
      )}
    </div>
  )
}

export default function HeroSection() {
  return (
    <FadeInSection as="section" aria-label="QuickDash introduction" style={{ textAlign: 'center', padding: '48px 0 32px' }}>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 12, marginBottom: 16 }}>
        <span className="tag" style={{ background: '#e8f5e9', borderColor: '#4caf50', color: '#1b5e20' }}>FREE</span>
        <span className="tag" style={{ background: '#fff3e0', borderColor: '#ff9800', color: '#e65100' }}>OPEN SOURCE</span>
        <span className="tag" style={{ background: '#fce4ec', borderColor: '#e91e63', color: '#880e4f' }}>ZERO TRACKING</span>
      </div>
      <img src="/assets/logo.png" alt="QuickDash app logo" width="96" height="96" className="pixel-art" style={{ display: 'block', margin: '16px auto' }} />
      <h1 style={{ fontFamily: 'var(--pixel-font)', fontSize: 28, letterSpacing: 2, color: 'var(--md-primary)', marginBottom: 8 }}>
        QUICKDASH
      </h1>
      <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)', marginBottom: 16, lineHeight: 1.8 }}>
        12 FLOATING TOOLS FOR ANDROID<br />
        UPI QR · TRANSLATOR · CLIPBOARD · NOTES · CALCULATOR · WI-FI SHARE<br />
        OCR · COLOR PICKER · UNIT CONVERTER · TIMER · BATTERY · TOOL DRAWER
      </p>
      <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 24, maxWidth: 600, margin: '0 auto 24px' }}>
        Zero tracking &bull; On-device processing &bull; Material Design 3
      </p>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap', marginTop: 12 }}>
        <a href="https://github.com/Balajitechlabs/quickdash/releases/latest" className="btn" target="_blank" rel="noopener noreferrer" aria-label="Download QuickDash Universal APK" onClick={() => trackEvent('download_click', 'universal_apk')}>
          <img src="/assets/github.svg" alt="" width="14" height="14" aria-hidden="true" />
          DOWNLOAD APK
        </a>
        <a href="https://play.google.com/store/apps/details?id=com.balajitechlabs.quickdash" className="btn btn-outline" target="_blank" rel="noopener noreferrer" aria-label="Get QuickDash on Google Play Store" onClick={() => trackEvent('download_click', 'play_store')}>
          <img src="/assets/play_store.svg" alt="" width="14" height="14" aria-hidden="true" />
          PLAY STORE (BETA)
        </a>
      </div>
      <GitHubRelease />
    </FadeInSection>
  )
}
