import { Helmet } from 'react-helmet-async'
import { useApi } from '../hooks/useApi'

function SkeletonCard() {
  return (
    <div className="card" style={{ padding: 24 }}>
      <div className="skeleton" style={{ width: '40%', height: 16, marginBottom: 8 }} />
      <div className="skeleton" style={{ width: '20%', height: 12, marginBottom: 12 }} />
      <div className="skeleton" style={{ width: '100%', height: 48 }} />
    </div>
  )
}

export default function Blog() {
  const { data: posts, loading, error } = useApi('/api/reading/posts.json')

  return (
    <>
      <Helmet>
        <title>Reading — QuickDash Blog</title>
        <meta name="description" content="Development blog, release notes, and technical articles about QuickDash and Android development." />
        <meta property="og:title" content="Reading — QuickDash Blog" />
        <meta property="og:description" content="Development blog, release notes, and technical articles about QuickDash and Android development." />
        <link rel="canonical" href="https://quickdash.balajitechlab.com/reading" />
      </Helmet>
      <div className="container" style={{ paddingTop: 32, paddingBottom: 48 }} role="status" aria-live="polite">
        <header>
          <h1 className="section-title" style={{ fontSize: 20 }}>Reading</h1>
          <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
            Development blog, release notes, and technical articles about QuickDash and Android development.
          </p>
        </header>

      {error && (
        <div className="card" style={{ textAlign: 'center', padding: 32, borderColor: 'var(--md-error)' }}>
          <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-error)' }}>ERROR LOADING POSTS</p>
          <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginTop: 8 }}>{error}</p>
        </div>
      )}

      <section aria-label="Blog posts">
        {loading ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {posts.length === 0 && !error && (
              <div className="card" style={{ textAlign: 'center', padding: 32 }}>
                <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)' }}>NO POSTS YET</p>
                <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginTop: 8 }}>Check back soon for new content.</p>
              </div>
            )}
            {posts.map(p => (
              <article key={p.id || p.slug} className="card">
                <h2 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-primary)', marginBottom: 4 }}>
                  {p.title}
                </h2>
                {p.date && (
                  <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-on-surface-variant)', marginBottom: 8 }}>
                    {new Date(p.date).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                  </p>
                )}
                {p.excerpt && (
                  <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)' }}>{p.excerpt}</p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>
      </div>
    </>
  )
}
