export default function Privacy() {
  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 48 }}>
      <header>
        <h1 className="section-title" style={{ fontSize: 20 }}>Privacy Policy</h1>
        <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
          Last updated: July 30, 2026
        </p>
      </header>

      <section aria-label="Privacy policy content" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        {sections.map((s, i) => (
          <article key={i} className="card">
            <h2 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-primary)', marginBottom: 8 }}>
              {s.title}
            </h2>
            {Array.isArray(s.content) ? (
              <ul style={{ paddingLeft: 20, fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.8 }}>
                {s.content.map((c, j) => <li key={j}>{c}</li>)}
              </ul>
            ) : (
              <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.7 }}>{s.content}</p>
            )}
          </article>
        ))}
      </section>
    </div>
  )
}

const sections = [
  {
    title: 'Data Collection — NONE',
    content: 'QuickDash collects absolutely no personal or device data. The app runs entirely on-device. No analytics, no telemetry, no crash reporting, no user tracking of any kind. QuickDash makes zero network requests from the app itself for data collection purposes.',
  },
  {
    title: 'Permissions',
    content: [
      'SYSTEM_ALERT_WINDOW (Overlay): Displays the floating tool dashboard over other apps. No data accessed or transmitted.',
      'Camera: Used exclusively for scanning UPI QR codes. Processed on-device. Never recorded or uploaded.',
      'Media Projection / Screen Recording: Used only when you explicitly initiate recording. Saved locally to your device.',
      'Internet: Used solely for optional features — app update checks, on-device AI model downloads, and currency conversion rates.',
      'Notifications (Android 13+): Used for timer alerts. No data collected.',
      'Vibration: Used for haptic feedback. No data collected.',
    ],
  },
  {
    title: 'Data Sharing',
    content: 'QuickDash does not share any data with third parties. No third-party analytics, advertising, or tracking SDKs are integrated.',
  },
  {
    title: 'Third-Party Services',
    content: [
      'Website (quickdash.balajitechlab.com) is hosted on GitHub Pages — GitHub may collect standard server logs.',
      'The website uses Google Analytics (GA4) for anonymous page view measurement. The app itself does not use GA4.',
      'Feedback form sends messages via a Telegram bot. This is an optional, user-initiated transmission.',
      'APK update checks fetch a static JSON file from the website with no identifiers.',
    ],
  },
  {
    title: 'Data Retention & Deletion',
    content: 'Since QuickDash collects no data, there is nothing to retain or delete. Local app data (notes, clipboard history, settings) can be cleared via app Settings or by uninstalling the app.',
  },
  {
    title: 'Children\'s Privacy',
    content: 'QuickDash does not collect personal information from anyone, including children under 13. If contacted via the feedback form, reach out at quickdash@balajitechlab.com and we will delete your information promptly.',
  },
  {
    title: 'Contact',
    content: 'For privacy-related questions: quickdash@balajitechlab.com or open an issue at github.com/Balajitechlabs/quickdash.',
  },
]