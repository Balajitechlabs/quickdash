import { useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import FadeInSection from '../components/FadeInSection'
import { trackEvent } from '../utils/analytics'

function DownloadCard({ icon, title, desc, github, playStore, href: customHref, label, wide }) {
  const href = customHref || (github
    ? 'https://github.com/balajitechlabs/quickdash/releases/latest'
    : 'https://play.google.com/store/apps/details?id=com.balajitechlabs.quickdash')
  const btnLabel = label || (github ? 'DOWNLOAD' : 'PLAY STORE')
  const imgSrc = github ? '/assets/github.svg' : '/assets/play_store.svg'
  const trackLabel = customHref ? 'foss_apk' : (github ? (label === 'DOWNLOAD ARM64' ? 'arm64_apk' : 'universal_apk') : 'play_store')

  return (
    <div className="card" style={{ textAlign: 'center', ...(wide ? { gridColumn: '1 / -1' } : {}) }}>
      <div style={{ fontSize: 48, marginBottom: 8 }}>{icon}</div>
      <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-primary)', marginBottom: 8 }}>{title}</h3>
      <p style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', marginBottom: 16 }}>{desc}</p>
      <a href={href} className={`btn btn-sm${playStore ? ' btn-outline' : ''}`} target="_blank" rel="noopener noreferrer" aria-label={`Download QuickDash ${title}`} onClick={() => trackEvent('download_click', trackLabel)}>
        <img src={imgSrc} alt="" width="12" height="12" aria-hidden="true" />
        {btnLabel}
      </a>
    </div>
  )
}

function FeedbackForm() {
  const [sent, setSent] = useState(false)

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault()
    const form = e.target
    const name = form.elements['name'].value
    const message = form.elements['message'].value
    trackEvent('feedback_submit', 'github_issues')
    const body = `**Feedback from ${name}**\n\n${message}`
    const url = `https://github.com/balajitechlabs/quickdash/issues/new?title=Feedback: ${encodeURIComponent(name)}&body=${encodeURIComponent(body)}&labels=feedback`
    window.open(url, '_blank', 'noopener')
    setSent(true)
  }, [])

  if (sent) {
    return (
      <FadeInSection as="section">
        <h2 className="section-title">Feedback</h2>
        <div className="card" style={{ textAlign: 'center', padding: 32 }}>
          <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 14, color: 'var(--md-primary)', marginBottom: 8 }}>✓ THANK YOU</p>
          <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 16 }}>GitHub Issues opened in a new tab. Submit it there to reach us!</p>
          <a href="https://github.com/balajitechlabs/quickdash/issues/new" target="_blank" rel="noopener noreferrer" className="btn btn-sm" onClick={() => trackEvent('feedback_submit', 'github_issues_direct')}>Open Issues Directly</a>
        </div>
      </FadeInSection>
    )
  }

  return (
    <FadeInSection as="section" aria-label="Send feedback to QuickDash developers">
      <h2 className="section-title">Feedback</h2>
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 13, marginBottom: 4 }}>
          Submit feedback directly to our GitHub Issues tracker.
        </p>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label htmlFor="feedback-name" className="sr-only">Your name</label>
            <input id="feedback-name" type="text" name="name" placeholder="Your name" required
              style={{ width: '100%', padding: '12px 16px', border: '1px solid var(--md-outline)', borderRadius: 6, fontFamily: 'var(--body-font)', fontSize: 14, background: 'var(--md-surface)', color: 'var(--md-on-surface)' }} />
          </div>
          <div>
            <label htmlFor="feedback-message" className="sr-only">Your message</label>
            <textarea id="feedback-message" name="message" rows={4} placeholder="Bug report, feature request, or general feedback" required
              style={{ width: '100%', padding: '12px 16px', border: '1px solid var(--md-outline)', borderRadius: 6, fontFamily: 'var(--body-font)', fontSize: 14, resize: 'vertical', background: 'var(--md-surface)', color: 'var(--md-on-surface)' }} />
          </div>
          <button type="submit" className="btn" style={{ alignSelf: 'flex-start' }}>
            <img src="/assets/github.svg" alt="" width="14" height="14" aria-hidden="true" />
            SUBMIT ON GITHUB
          </button>
        </form>
        <p style={{ fontSize: 11, color: 'var(--md-on-surface-variant)', textAlign: 'center' }}>
          Prefer Telegram? <a href="https://t.me/balajitechlabs" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--md-primary)' }} onClick={() => trackEvent('contact_click', 'telegram')}>Message us here</a>
        </p>
      </div>
    </FadeInSection>
  )
}

export default function CtaSection() {
  return (
    <>
      <FadeInSection as="section" aria-labelledby="download-title">
        <h2 id="download-title" className="section-title">Download</h2>
        <div className="grid-2">
          <DownloadCard icon="📦" title="Universal APK" desc="Compatible with all Android 7.0+ devices." github />
          <DownloadCard icon="🌿" title="FOSS Edition" desc="100% Zero-Tracker APK for F-Droid & privacy enthusiasts." github href="https://github.com/balajitechlabs/quickdash/releases/latest/download/app-foss-release.apk" label="DOWNLOAD FOSS" />
          <DownloadCard icon="⚙️" title="ARM64 APK" desc="Optimized for ARM64 devices. Smaller APK size." github label="DOWNLOAD ARM64" />
          <DownloadCard icon="▶️" title="Play Store Beta" desc="Join the Google Play Beta program." playStore />
        </div>
      </FadeInSection>

      <FeedbackForm />

      <FadeInSection as="section" aria-label="Get help and support">
        <h2 className="section-title">Get Help</h2>
        <div className="card" style={{ textAlign: 'center', padding: 24 }}>
          <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', marginBottom: 16, lineHeight: 1.7 }}>
            Found a bug? Have a feature request? Need help using QuickDash?
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap' }}>
            <a href="https://github.com/balajitechlabs/quickdash/issues/new" target="_blank" rel="noopener noreferrer" className="btn btn-sm" onClick={() => trackEvent('contact_click', 'github_issues')}>
              🐛 GitHub Issues
            </a>
            <a href="https://t.me/balajitechlabs" target="_blank" rel="noopener noreferrer" className="btn btn-sm btn-outline" onClick={() => trackEvent('contact_click', 'telegram')}>
              💬 Telegram
            </a>
            <a href="mailto:quickdash@balajitechlab.com" className="btn btn-sm btn-outline" onClick={() => trackEvent('contact_click', 'email')}>
              ✉️ Email
            </a>
          </div>
        </div>
      </FadeInSection>

      <FadeInSection as="section" aria-labelledby="privacy-title" style={{ paddingBottom: 32 }}>
        <h2 id="privacy-title" className="section-title">Privacy</h2>
        <div className="card">
          <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 12 }}>
            QuickDash collects zero data. No analytics, no telemetry, no network requests from the app itself. Everything runs on-device.
          </p>
          <Link to="/privacy" className="btn btn-sm btn-outline">Full Privacy Policy</Link>
        </div>
      </FadeInSection>

      <FadeInSection as="section" style={{ paddingBottom: 32 }}>
        <h2 className="section-title">Credits</h2>
        <div className="card" style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', lineHeight: 1.7 }}>
          <p>QuickDash is a fork of <strong>PocketOps</strong> by <strong>Aakarsh (L192) / IIXII™</strong> under the PocketOps Custom Open Source Fork License.</p>
          <p style={{ marginTop: 8 }}>
            View the original project: <a href="https://github.com/IIXII-L192/PocketOps-app" target="_blank" rel="noopener noreferrer" onClick={() => trackEvent('external_link', 'pocketops_github')}>github.com/IIXII-L192/PocketOps-app</a>
          </p>
        </div>
      </FadeInSection>
    </>
  )
}
