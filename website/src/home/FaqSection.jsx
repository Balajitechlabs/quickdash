import { useState } from 'react'
import FadeInSection from '../components/FadeInSection'
import { trackEvent } from '../utils/analytics'

const faqs = [
  { q: 'What is QuickDash?', a: 'QuickDash is a floating overlay utility hub for Android featuring 12 tools — UPI QR, translator, clipboard, notes, calculator, Wi-Fi sharing, and more. All tools run on-device with zero tracking.' },
  { q: 'Is QuickDash free?', a: 'Yes, QuickDash is 100% free and open source, with no ads or telemetry of any kind.' },
  { q: 'Does it collect my data?', a: 'No. QuickDash collects zero data. No analytics, no telemetry, no network requests. Everything runs entirely on your device.' },
  { q: 'Does it work on Android 16?', a: 'Yes, QuickDash v5.1.1 targets Android 16 (SDK 36) with 16KB page alignment for full compatibility.' },
  { q: 'Does it require root?', a: 'No. QuickDash uses the standard SYSTEM_ALERT_WINDOW overlay permission. Works on stock non-rooted Android 7.0 through Android 16.' },
  { q: 'How do I update?', a: 'The app checks for updates automatically on launch. Tap the version badge in the header or go to Settings > About > Check for Updates to download the latest APK directly.' },
  { q: 'Can I customize which tools appear?', a: 'Yes. Open the Tool Drawer, tap the edit icon, and toggle which tools you want visible in your floating dock.' },
  { q: 'How do I uninstall?', a: 'Long-press the app icon and drag to Uninstall, or go to Settings > Apps > QuickDash > Uninstall.' },
  { q: 'How do I enable overlay permission?', a: 'Go to Settings > Apps > QuickDash > Display over other apps and toggle it on. The app will guide you if needed.' },
  { q: 'How do I report a bug?', a: 'Open a GitHub Issue at github.com/balajitechlabs/quickdash/issues, message us on Telegram, or email quickdash@balajitechlab.com.' },
]

export default function FaqSection() {
  const [openIdx, setOpenIdx] = useState(null)

  return (
    <FadeInSection as="section" aria-label="Frequently asked questions">
      <h2 className="section-title">FAQ</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {faqs.map((f, i) => (
          <div key={i} className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <button
              onClick={() => {
                setOpenIdx(openIdx === i ? null : i)
                trackEvent('faq_toggle', openIdx === i ? 'close' : 'open')
              }}
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
