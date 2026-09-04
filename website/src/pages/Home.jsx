import { useState, useEffect } from 'react'
import { Helmet } from 'react-helmet-async'
import HeroSection from '../home/HeroSection'
import StatsBar from '../components/StatsBar'
import FeatureGrid from '../home/FeatureGrid'
import SpecsTable from '../home/SpecsTable'
import ThemeShowcase from '../home/ThemeShowcase'
import TestimonialCarousel from '../home/TestimonialCarousel'
import FaqSection from '../home/FaqSection'
import GallerySection from '../home/GallerySection'
import CtaSection from '../home/CtaSection'
import UpdateBanner from '../home/UpdateBanner'
import FadeInSection from '../components/FadeInSection'
import { trackEvent } from '../utils/analytics'

function ChangelogSection() {
  const [releases, setReleases] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/api/reading/changelogs.json')
      .then(r => r.ok ? r.json() : [])
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          setReleases(data.slice(0, 3))
        } else {
          // Fallback to GitHub Releases API
          return fetch('https://api.github.com/repos/balajitechlabs/quickdash/releases?per_page=5')
            .then(r => r.ok ? r.json() : [])
            .then(d => setReleases(d.slice(0, 3)))
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <FadeInSection as="section" aria-label="Recent releases changelog">
      <h2 className="section-title">Changelog</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {loading && <p className="card" style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', textAlign: 'center', padding: 24 }}>Loading releases...</p>}
        {!loading && releases.length === 0 && (
          <p className="card" style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', textAlign: 'center', padding: 24 }}>No recent release notes available.</p>
        )}
        {releases.map((r, idx) => (
          <div key={r.id || r.version || idx} className="card" style={{ padding: 16 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 8, flexWrap: 'wrap', gap: 4 }}>
              <span style={{ fontFamily: 'var(--pixel-font)', fontSize: 9, color: 'var(--md-primary)' }}>
                v{r.version || (r.tag_name ? r.tag_name.replace('v', '') : '')}
              </span>
              <span style={{ fontSize: 11, color: 'var(--md-on-surface-variant)' }}>
                {r.date || (r.published_at ? new Date(r.published_at).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '')}
              </span>
            </div>
            <p style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', lineHeight: 1.6, margin: 0 }}>
              {r.notes || (r.highlights ? r.highlights.join(' • ') : (r.body ? r.body.split('\n').slice(0, 4).join('\n') : ''))}
            </p>
          </div>
        ))}
        <a href="/changelog" className="btn btn-sm btn-outline" style={{ alignSelf: 'center', marginTop: 8 }} onClick={() => trackEvent('changelog_click', 'all_releases')}>
          View full changelog
        </a>
      </div>
    </FadeInSection>
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
    <>
      <Helmet>
        <title>QuickDash — Floating Tools for Android</title>
        <meta name="description" content="QuickDash is a floating overlay utility hub for Android. 12 tools — UPI QR, translator, clipboard, notes, calculator, Wi-Fi sharing, OCR, color picker, and more. Zero tracking, on-device processing." />
        <meta name="keywords" content="QuickDash, floating tools, Android utility, UPI QR, clipboard manager, floating calculator, notes app, Android overlay, open source, zero tracking" />
        <meta property="og:title" content="QuickDash — Floating Tools for Android" />
        <meta property="og:description" content="12 floating tools for Android. Zero tracking, on-device processing, Material Design 3." />
        <meta property="og:image" content="/assets/og-image.png" />
        <meta property="og:url" content="https://quickdash.balajitechlab.com" />
        <meta name="twitter:card" content="summary_large_image" />
        <link rel="canonical" href="https://quickdash.balajitechlab.com" />
      </Helmet>

      <div className="container page-transition">
        <HeroSection />
        <UpdateBanner />

        <div className="pixel-divider" role="separator" />

        <FadeInSection as="section" aria-label="QuickDash statistics">
          <h2 className="section-title">Stats</h2>
          <StatsBar />
        </FadeInSection>

        <div className="pixel-divider" role="separator" />
        <FeatureGrid />

        <div className="pixel-divider" role="separator" />
        <ThemeShowcase onPreview={setPreview} />

        <div className="pixel-divider" role="separator" />
        <SpecsTable />

        <div className="pixel-divider" role="separator" />
        <CtaSection />

        <div className="pixel-divider" role="separator" />
        <TestimonialCarousel />

        <div className="pixel-divider" role="separator" />
        <GallerySection />

        <div className="pixel-divider" role="separator" />
        <ChangelogSection />

        <div className="pixel-divider" role="separator" />
        <FaqSection />
      </div>
    </>
  )
}
