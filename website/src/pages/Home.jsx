import { useState, useRef, useEffect, useCallback, createContext, useContext, useMemo } from 'react'

const features = [
  { id: 'translator', name: 'Translator', icon: '🔤', desc: 'Instant on-screen translation between 100+ languages. Runs on-device, no data sent to the cloud.' },
  { id: 'clipboard', name: 'Clipboard', icon: '📋', desc: 'Smart clipboard manager with history, favorites, and one-tap paste into any app.' },
  { id: 'qr', name: 'UPI QR', icon: '📱', desc: 'Generate UPI QR codes from any screen. Tap to scan or share.' },
  { id: 'notes', name: 'Notes', icon: '📝', desc: 'Quick notes that float on top of your current app. Syncs with system clipboard.' },
  { id: 'calc', name: 'Calculator', icon: '🔢', desc: 'Floating calculator for quick arithmetic without switching apps.' },
  { id: 'wifi', name: 'Wi-Fi Share', icon: '📶', desc: 'Share connected Wi-Fi credentials as QR codes instantly.' },
  { id: 'unit', name: 'Unit Converter', icon: '⚖️', desc: 'Convert length, weight, temperature, currency, and more.' },
  { id: 'text', name: 'Text Extractor', icon: '👁️', desc: 'Extract text from images using on-device OCR.' },
  { id: 'color', name: 'Color Picker', icon: '🎨', desc: 'Pick colors from any screen area and copy hex/RGB values.' },
  { id: 'timer', name: 'Timer', icon: '⏱️', desc: 'Floating countdown timer with preset intervals.' },
  { id: 'battery', name: 'Battery Info', icon: '🔋', desc: 'View battery stats, health, and estimated remaining time.' },
  { id: 'tools', name: 'Tool Drawer', icon: '🧰', desc: 'Quick-access drawer for all your favorite utilities in one place.' },
]

const specs = [
  { label: 'Min SDK', value: 'Android 7.0 (API 24)' },
  { label: 'Target SDK', value: 'Android 16 (API 36)' },
  { label: 'Architecture', value: 'Universal / ARM64' },
  { label: 'License', value: 'Custom OS Fork License' },
  { label: 'Tracking', value: 'Zero — no analytics, no telemetry' },
  { label: 'Languages', value: '100+ via on-device translator' },
  { label: 'ABI Split', value: 'Universal + ARM64 builds' },
  { label: 'Page Size', value: '16KB aligned (Android 16)' },
]

const themes = [
  { name: 'Pixel Purple', colors: { primary: '#7c4dff', surface: '#fffbfe' } },
  { name: 'Matrix Green', colors: { primary: '#00c853', surface: '#f5fff5' } },
  { name: 'Sunset Orange', colors: { primary: '#ff6d00', surface: '#fff8f3' } },
  { name: 'Ocean Blue', colors: { primary: '#2979ff', surface: '#f5f9ff' } },
]

const testimonials = [
  { name: 'Rahul K.', role: 'Beta Tester', device: 'Pixel 8 Pro', text: 'The floating translator is a game-changer. I use it daily for reading articles in other languages.' },
  { name: 'Priya M.', role: 'Android User', device: 'Samsung S24 Ultra', text: 'Finally an overlay tool that doesn\'t drain battery. Zero tracking is a huge plus.' },
  { name: 'Arun S.', role: 'Beta Tester', device: 'OnePlus 12', text: 'UPI QR floating over any app is insane. I don\'t need to switch apps to pay anymore.' },
]

const ThemeCtx = createContext({ preview: null, setPreview: () => {} })

function useScrollReveal() {
  const ref = useRef(null)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    const observer = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) el.classList.add('visible') },
      { threshold: 0.1 }
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [])
  return ref
}

function FadeInSection({ children, as: Tag = 'div', ...props }) {
  const ref = useScrollReveal()
  return <Tag ref={ref} className="fade-in" {...props}>{children}</Tag>
}

function Screenshot({ src, alt, priority, onOpen }) {
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState(false)

  if (error) {
    return (
      <div className="card" style={{ padding: 16, textAlign: 'center', aspectRatio: '9/19', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}>
        <span style={{ fontSize: 32 }}>📱</span>
        <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 7, color: 'var(--md-on-surface-variant)' }}>LOAD FAILED</p>
      </div>
    )
  }

  return (
    <div className="card" style={{ padding: 4, cursor: 'pointer', opacity: loaded ? 1 : 0.5 }} onClick={() => onOpen(src)} role="button" tabIndex={0} onKeyDown={(e) => e.key === 'Enter' && onOpen(src)} aria-label="Open screenshot full-size">
      {!loaded && <div className="skeleton" style={{ width: '100%', aspectRatio: '9/19' }} />}
      <picture>
        <source srcSet={src.replace('.png', '.webp')} type="image/webp" />
        <img
          src={src}
          alt={alt}
          loading={priority ? 'eager' : 'lazy'}
          fetchpriority={priority ? 'high' : undefined}
          width="200"
          height="420"
          onLoad={() => setLoaded(true)}
          onError={() => setError(true)}
          style={{ display: 'block', width: '160px', borderRadius: 4 }}
        />
      </picture>
    </div>
  )
}

function Lightbox({ src, onClose }) {
  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', handleKey)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKey)
      document.body.style.overflow = ''
    }
  }, [onClose])

  return (
    <div
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Image fullscreen view"
      style={{
        position: 'fixed', inset: 0, zIndex: 9999,
        background: 'rgba(0,0,0,0.85)', display: 'flex',
        alignItems: 'center', justifyContent: 'center',
        cursor: 'zoom-out', animation: 'fadeIn 0.2s ease',
        padding: 20,
      }}
    >
      <img
        src={src}
        alt="Full size screenshot"
        style={{ maxWidth: '100%', maxHeight: '90vh', borderRadius: 8, boxShadow: '0 8px 40px rgba(0,0,0,0.5)' }}
      />
      <button
        onClick={onClose}
        aria-label="Close lightbox"
        style={{
          position: 'absolute', top: 20, right: 20,
          background: 'none', border: 'none', color: 'white',
          fontSize: 32, cursor: 'pointer', fontFamily: 'var(--body-font)',
          opacity: 0.8, width: 48, height: 48, display: 'flex',
          alignItems: 'center', justifyContent: 'center',
          borderRadius: '50%', background: 'rgba(255,255,255,0.1)',
        }}
      >
        ✕
      </button>
    </div>
  )
}

function FAQ() {
  const faqs = [
    { q: 'What is QuickDash?', a: 'QuickDash is a floating overlay utility hub for Android featuring 12 tools — UPI QR, translator, clipboard, notes, calculator, Wi-Fi sharing, and more. All tools run on-device with zero tracking.' },
    { q: 'Is QuickDash free?', a: 'Yes, QuickDash is 100% free and open source, with no ads or telemetry of any kind.' },
    { q: 'Does it work on Android 16?', a: 'Yes, QuickDash v5.1.1 targets Android 16 (SDK 36) with 16KB page alignment for full compatibility.' },
    { q: 'Does it require root?', a: 'No. QuickDash uses the standard SYSTEM_ALERT_WINDOW overlay permission. Works on stock non-rooted Android 7.0 through Android 16.' },
    { q: 'How do I uninstall?', a: 'Long-press the app icon and drag to Uninstall, or go to Settings > Apps > QuickDash > Uninstall.' },
    { q: 'How do I enable overlay permission?', a: 'Go to Settings > Apps > QuickDash > Display over other apps and toggle it on. The app will guide you if needed.' },
  ]
  const [openIdx, setOpenIdx] = useState(0)

  return (
    <FadeInSection as="section" aria-label="Frequently asked questions">
      <h2 className="section-title">FAQ</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {faqs.map((f, i) => (
          <div key={i} className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <button
              onClick={() => setOpenIdx(openIdx === i ? null : i)}
              aria-expanded={openIdx === i}
              aria-controls={`faq-answer-${i}`}
              style={{
                width: '100%', textAlign: 'left', padding: '16px 20px', border: 'none',
                background: openIdx === i ? 'var(--md-primary-container)' : 'transparent',
                cursor: 'pointer', fontFamily: 'var(--pixel-font)', fontSize: 10,
                color: openIdx === i ? 'var(--md-on-primary-container)' : 'var(--md-on-surface)',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              }}
            >
              {f.q}
              <span style={{ transform: openIdx === i ? 'rotate(180deg)' : 'none', transition: 'transform 0.15s', fontSize: 14 }}>
                ▼
              </span>
            </button>
            <div
              id={`faq-answer-${i}`}
              role="region"
              hidden={openIdx !== i}
              style={{ padding: openIdx === i ? '0 20px 16px' : '0 20px', maxHeight: openIdx === i ? '200px' : '0', overflow: 'hidden', transition: 'max-height 0.2s, padding 0.2s' }}
            >
              <p style={{ fontFamily: 'var(--body-font)', fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.7 }}>
                {f.a}
              </p>
            </div>
          </div>
        ))}
      </div>
    </FadeInSection>
  )
}

function Gallery() {
  const [lightbox, setLightbox] = useState(null)
  const total = 15
  const images = useMemo(() => Array.from({ length: total }, (_, i) => ({
    src: `/assets/gallery/shot_${i + 1}.png`,
    alt: `QuickDash screenshot ${i + 1} — feature overview`,
  })), [])

  return (
    <FadeInSection as="section" aria-label="QuickDash screenshots gallery">
      <h2 className="section-title">Gallery</h2>
      <div style={{ display: 'flex', gap: 12, overflowX: 'auto', paddingBottom: 12, scrollSnapType: 'x mandatory', WebkitOverflowScrolling: 'touch' }}>
        {images.map((img, i) => (
          <div key={i} style={{ flex: '0 0 auto', scrollSnapAlign: 'start' }}>
            <Screenshot src={img.src} alt={img.alt} priority={i < 3} onOpen={setLightbox} />
          </div>
        ))}
      </div>
      {lightbox && <Lightbox src={lightbox} onClose={() => setLightbox(null)} />}
    </FadeInSection>
  )
}

function FeedbackForm() {
  const [sent, setSent] = useState(false)

  const handleSubmit = useCallback(async (e) => {
    e.preventDefault()
    const form = e.target
    const name = form.name.value
    const message = form.message.value
    const body = `**Feedback from ${name}**\n\n${message}`
    const url = `https://github.com/Balajitechlabs/quickdash/issues/new?title=Feedback: ${encodeURIComponent(name)}&body=${encodeURIComponent(body)}&labels=feedback`
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
          <a href="https://github.com/Balajitechlabs/quickdash/issues/new" target="_blank" rel="noopener noreferrer" className="btn btn-sm">Open Issues Directly</a>
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
          Prefer Telegram? <a href="https://t.me/BalajiTechLabs" target="_blank" rel="noopener noreferrer" style={{ color: 'var(--md-primary)' }}>Message us here</a>
        </p>
      </div>
    </FadeInSection>
  )
}

function ThemePreview({ name, colors, onApply }) {
  return (
    <button
      onClick={() => onApply(colors)}
      className="card"
      style={{ padding: 16, cursor: 'pointer', textAlign: 'center', background: 'var(--md-surface)', border: '1px solid var(--md-outline)' }}
      title={`Apply ${name} theme`}
      aria-label={`Apply ${name} theme`}
    >
      <div style={{ display: 'flex', gap: 8, justifyContent: 'center', marginBottom: 8 }}>
        <div style={{ width: 24, height: 24, background: colors.primary, border: '2px solid var(--md-outline)', borderRadius: 4 }} />
        <div style={{ width: 24, height: 24, background: colors.surface, border: '2px solid var(--md-outline)', borderRadius: 4 }} />
      </div>
      <span style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-on-surface)' }}>{name}</span>
    </button>
  )
}

function Hero() {
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
        <a href="https://github.com/Balajitechlabs/quickdash/releases/latest" className="btn" target="_blank" rel="noopener noreferrer" aria-label="Download QuickDash Universal APK">
          <img src="/assets/github.svg" alt="" width="14" height="14" aria-hidden="true" />
          DOWNLOAD APK
        </a>
        <a href="https://play.google.com/store/apps/details?id=com.balajitechlabs.mischief" className="btn btn-outline" target="_blank" rel="noopener noreferrer" aria-label="Get QuickDash on Google Play Store">
          <img src="/assets/play_store.svg" alt="" width="14" height="14" aria-hidden="true" />
          PLAY STORE (BETA)
        </a>
      </div>
      <GitHubRelease />
    </FadeInSection>
  )
}

function GitHubRelease() {
  const [release, setRelease] = useState(null)
  const [commit, setCommit] = useState(null)

  useEffect(() => {
    fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases/latest')
      .then(r => r.ok ? r.json() : null)
      .then(d => setRelease(d))
      .catch(() => {})
  }, [])

  useEffect(() => {
    fetch('https://api.github.com/repos/Balajitechlabs/quickdash/commits/main')
      .then(r => r.ok ? r.json() : null)
      .then(d => setCommit(d))
      .catch(() => {})
  }, [])

  return (
    <div style={{ marginTop: 20, display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap', fontFamily: 'var(--body-font)', fontSize: 13 }}>
      <a href="https://github.com/Balajitechlabs/quickdash/actions" target="_blank" rel="noopener noreferrer" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: 'var(--md-on-surface-variant)', textDecoration: 'none', background: 'var(--md-surface-variant)', padding: '4px 10px', borderRadius: 20, fontSize: 11 }}>
        <span style={{ display: 'inline-block', width: 8, height: 8, borderRadius: '50%', background: '#4caf50' }} />
        Build passing
      </a>
      <a href="https://github.com/Balajitechlabs/quickdash/blob/main/LICENSE" target="_blank" rel="noopener noreferrer" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: 'var(--md-on-surface-variant)', textDecoration: 'none', background: 'var(--md-surface-variant)', padding: '4px 10px', borderRadius: 20, fontSize: 11 }}>
        <span style={{ display: 'inline-block', width: 8, height: 8, borderRadius: '50%', background: '#7c4dff' }} />
        Custom OS Fork License
      </a>
      {release && (
        <a href={release.html_url} target="_blank" rel="noopener noreferrer" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: 'var(--md-on-surface-variant)', textDecoration: 'none', background: 'var(--md-surface-variant)', padding: '4px 10px', borderRadius: 20, fontSize: 11 }}>
          <span style={{ display: 'inline-block', width: 8, height: 8, borderRadius: '50%', background: '#4caf50', animation: 'pulse 2s infinite' }} />
          v{release.tag_name.replace('v', '')} — {new Date(release.published_at).toLocaleDateString()}
        </a>
      )}
      {commit && (
        <a href={commit.html_url} target="_blank" rel="noopener noreferrer" style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: 'var(--md-on-surface-variant)', textDecoration: 'none', background: 'var(--md-surface-variant)', padding: '4px 10px', borderRadius: 20, fontSize: 11 }}>
          <span style={{ fontFamily: 'var(--mono-font)', fontSize: 10 }}>{commit.sha.slice(0, 7)}</span>
        </a>
      )}
    </div>
  )
}

export default function Home() {
  const [preview, setPreview] = useState(null)

  useEffect(() => {
    if (preview) {
      document.documentElement.style.setProperty('--md-primary', preview.primary)
      document.documentElement.style.setProperty('--md-surface', preview.surface)
      return () => {
        document.documentElement.style.removeProperty('--md-primary')
        document.documentElement.style.removeProperty('--md-surface')
      }
    }
  }, [preview])

  return (
    <ThemeCtx.Provider value={{ preview, setPreview }}>
      <div className="container page-transition">
        <Hero />

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-label="QuickDash statistics">
          <h2 className="section-title">Stats</h2>
          <div className="grid-3">
            {stats.map(s => (
              <div key={s.label} className="card" style={{ textAlign: 'center' }}>
                <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 24, color: 'var(--md-primary)' }}>{s.value}</p>
                <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-on-surface-variant)', marginTop: 8 }}>{s.label}</p>
              </div>
            ))}
          </div>
        </FadeInSection>

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-labelledby="features-title">
          <h2 id="features-title" className="section-title">Features</h2>
          <div className="grid-3">
            {features.map(f => (
              <div key={f.id} className="card" style={{ textAlign: 'center' }}>
                <span role="img" aria-label={`${f.name} icon`} className="feature-icon" style={{ fontSize: 32, display: 'block', marginBottom: 8 }}>{f.icon}</span>
                <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 9, color: 'var(--md-primary)', marginBottom: 8 }}>{f.name}{['translator','qr','color','text'].includes(f.id) && <span className="demo-badge">DEMO</span>}</h3>
                <p style={{ fontSize: 13, color: 'var(--md-on-surface-variant)' }}>{f.desc}</p>
              </div>
            ))}
          </div>
        </FadeInSection>

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-label="Live theme preview">
          <h2 className="section-title">Theme Presets</h2>
          <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 13, marginBottom: 16 }}>
            Click a preset to preview. Lasts 3 seconds.
          </p>
          <ThemePresets />
        </FadeInSection>

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-labelledby="specs-title">
          <h2 id="specs-title" className="section-title">Tech Specs</h2>
          <div className="card" style={{ overflow: 'hidden', padding: 0, borderRadius: 8 }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <caption className="sr-only">QuickDash technical specifications</caption>
              <tbody>
                {specs.map((s, i) => (
                  <tr key={s.label} style={{ borderBottom: '1px solid var(--md-outline)' }}>
                    <td style={{ padding: '12px 20px', fontFamily: 'var(--pixel-font)', fontSize: 9, color: 'var(--md-primary)', background: 'var(--md-primary-container)', width: '40%', borderRight: '1px solid var(--md-outline)' }}>
                      {s.label}
                    </td>
                    <td style={{ padding: '12px 20px', fontFamily: 'var(--body-font)', fontSize: 14, color: 'var(--md-on-surface)' }}>
                      {s.value}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </FadeInSection>

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-labelledby="download-title">
          <h2 id="download-title" className="section-title">Download</h2>
          <div className="grid-2">
            <DownloadCard icon="📦" title="Universal APK" desc="Compatible with all Android 7.0+ devices." github />
            <DownloadCard icon="⚙️" title="ARM64 APK" desc="Optimized for ARM64 devices. Smaller APK size." github label="DOWNLOAD ARM64" />
            <DownloadCard icon="▶️" title="Play Store Beta" desc="Join the Google Play Beta program." playStore wide />
          </div>
        </FadeInSection>

        <div className="pixel-divider" role="separator" />

        <Testimonials />

        <div className="pixel-divider" role="separator" />

        <Gallery />

        <div className="pixel-divider" role="separator" />

        <FAQ />

        <div className="pixel-divider" role="separator" />

        <FeedbackForm />

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-labelledby="privacy-title" style={{ paddingBottom: 32 }}>
          <h2 id="privacy-title" className="section-title">Privacy</h2>
          <div className="card">
            <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 12 }}>
              QuickDash collects zero data. No analytics, no telemetry, no network requests from the app itself. Everything runs on-device.
            </p>
            <a href="/privacy" className="btn btn-sm btn-outline">Full Privacy Policy</a>
          </div>
        </FadeInSection>

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" style={{ paddingBottom: 32 }}>
          <h2 className="section-title">Credits</h2>
          <div className="card" style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', lineHeight: 1.7 }}>
            <p>QuickDash is a fork of <strong>PocketOps</strong> by <strong>Aakarsh (L192) / IIXII™</strong> under the PocketOps Custom Open Source Fork License.</p>
            <p style={{ marginTop: 8 }}>
              View the original project: <a href="https://github.com/L192/PocketOps" target="_blank" rel="noopener noreferrer">github.com/L192/PocketOps</a>
            </p>
          </div>
        </FadeInSection>
      </div>
    </ThemeCtx.Provider>
  )
}

const stats = [
  { label: 'DOWNLOADS', value: '10K+' },
  { label: 'TOOLS', value: '12' },
  { label: 'ACTIVE USERS', value: '5K+' },
]

function DownloadCard({ icon, title, desc, github, playStore, label, wide }) {
  const href = github
    ? 'https://github.com/Balajitechlabs/quickdash/releases/latest'
    : 'https://play.google.com/store/apps/details?id=com.balajitechlabs.mischief'
  const btnLabel = label || (github ? 'DOWNLOAD' : 'PLAY STORE')
  const imgSrc = github ? '/assets/github.svg' : '/assets/play_store.svg'

  return (
    <div className="card" style={{ textAlign: 'center', ...(wide ? { gridColumn: '1 / -1' } : {}) }}>
      <div style={{ fontSize: 48, marginBottom: 8 }}>{icon}</div>
      <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-primary)', marginBottom: 8 }}>{title}</h3>
      <p style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', marginBottom: 16 }}>{desc}</p>
      <a href={href} className={`btn btn-sm${playStore ? ' btn-outline' : ''}`} target="_blank" rel="noopener noreferrer" aria-label={`Download QuickDash ${title}`}>
        <img src={imgSrc} alt="" width="12" height="12" aria-hidden="true" />
        {btnLabel}
      </a>
    </div>
  )
}

function ThemePresets() {
  const { setPreview } = useContext(ThemeCtx)
  const [activeTheme, setActiveTheme] = useState(null)

  const applyTheme = useCallback((colors, name) => {
    setPreview(colors)
    setActiveTheme(name)
    setTimeout(() => {
      setPreview(null)
      setActiveTheme(null)
    }, 3000)
  }, [setPreview])

  return (
    <>
      <div className="grid-4">
        {themes.map(t => (
          <ThemePreview key={t.name} name={t.name} colors={t.colors} onApply={(c) => applyTheme(c, t.name)} />
        ))}
      </div>
      {activeTheme && (
        <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-primary)', marginTop: 12, textAlign: 'center', animation: 'fadeIn 0.2s' }}>
          Previewing: {activeTheme}
        </p>
      )}
    </>
  )
}

function Testimonials() {
  return (
    <FadeInSection as="section" aria-label="User testimonials">
      <h2 className="section-title">Testimonials</h2>
      <div className="grid-2">
        {testimonials.map((t, i) => (
          <div key={i} className="card" style={{ position: 'relative' }}>
            <span style={{ position: 'absolute', top: 12, left: 16, fontSize: 32, opacity: 0.15, fontFamily: 'serif' }}>"</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <div style={{
                width: 40, height: 40, borderRadius: '50%', background: 'var(--md-primary)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: 'var(--pixel-font)', fontSize: 14, color: 'var(--md-on-primary)',
                flexShrink: 0,
              }}>
                {t.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div>
                <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-primary)' }}>{t.name}</p>
                <p style={{ fontSize: 11, color: 'var(--md-on-surface-variant)' }}>{t.role}</p>
              </div>
            </div>
            <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.7, fontStyle: 'italic', marginBottom: 12 }}>{t.text}</p>
            {t.device && (
              <p style={{ fontSize: 11, color: 'var(--md-primary)', opacity: 0.6 }}>Tested on {t.device}</p>
            )}
          </div>
        ))}
      </div>
    </FadeInSection>
  )
}
