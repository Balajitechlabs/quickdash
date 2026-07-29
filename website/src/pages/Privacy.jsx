export default function Privacy() {
  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 48 }}>
      <header>
        <h1 className="section-title" style={{ fontSize: 20 }}>Privacy Policy</h1>
        <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
          Last updated: March 2026
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
    title: 'Data Collection',
    content: 'QuickDash collects absolutely no data. The app runs entirely on-device and makes zero network requests. No analytics, no telemetry, no crash reporting, no user tracking of any kind.',
  },
  {
    title: 'Permissions',
    content: 'QuickDash requires the SYSTEM_ALERT_WINDOW permission to display overlay tools. This permission is used solely for the app\'s core functionality. No data is accessed or transmitted via this or any other permission.',
  },
  {
    title: 'Third-Party Services',
    content: [
      'The QuickDash APK and website (quickdash.balajitechlab.com) are hosted on GitHub Pages. GitHub may collect standard server logs per their privacy policy.',
      'The website includes a feedback form powered by Formspree and Telegram. Submitting the form sends your message to the developer — no data is stored or shared beyond this transmission.',
      'No cookies, localStorage, or session storage is used by the website for tracking purposes.',
    ],
  },
  {
    title: 'Updates',
    content: 'The app checks for updates by fetching a static JSON file from the website. No device identifiers or personal information is included in this check.',
  },
  {
    title: 'Contact',
    content: 'For privacy-related questions, open an issue at github.com/Balajitechlabs/quickdash or use the feedback form on the website.',
  },
]
