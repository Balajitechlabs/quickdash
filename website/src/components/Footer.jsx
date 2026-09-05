import { Link } from 'react-router-dom'

export default function Footer() {
  const year = new Date().getFullYear()

  return (
    <footer role="contentinfo" style={{
      borderTop: '1px solid var(--md-outline)', marginTop: 48, padding: '32px 20px',
      textAlign: 'center',
    }}>
      <div style={{ maxWidth: 'var(--max-width)', margin: '0 auto' }}>
        <nav aria-label="Footer navigation" style={{ display: 'flex', justifyContent: 'center', gap: 24, flexWrap: 'wrap', marginBottom: 16 }}>
          <Link to="/privacy" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            Privacy
          </Link>
          <Link to="/docs" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            Docs
          </Link>
          <Link to="/changelog" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            Changelog
          </Link>
          <a href="https://github.com/balajitechlabs/quickdash" target="_blank" rel="noopener noreferrer" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            GitHub
          </a>
          <a href="https://github.com/balajitechlabs/quickdash/issues" target="_blank" rel="noopener noreferrer" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            Issues
          </a>
          <a href="https://github.com/balajitechlabs/quickdash/discussions" target="_blank" rel="noopener noreferrer" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            Discussions
          </a>
          <a href="https://play.google.com/store/apps/details?id=com.balajitechlabs.quickdash" target="_blank" rel="noopener noreferrer" style={{ fontFamily: 'var(--body-font)', fontSize: 13, color: 'var(--md-on-surface-variant)' }}>
            Play Store
          </a>
        </nav>
        <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-on-surface-variant)', lineHeight: 2 }}>
          &copy; {year} ||BTL||™ (balajitechlabs) &mdash; FREE TO USE &amp; OPEN SOURCE<br />
          Fork of IIXII™ property by Aakarsh (L192) &mdash; <a href="https://github.com/IIXII-L192/PocketOps-app" target="_blank" rel="noopener noreferrer" style={{color:'var(--md-primary)'}}>Original</a><br />
          NOT AFFILIATED WITH GOOGLE LLC. ANDROID IS A TRADEMARK OF GOOGLE LLC.
        </p>
      </div>
    </footer>
  )
}
