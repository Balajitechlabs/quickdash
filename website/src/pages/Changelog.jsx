import { useApi } from '../hooks/useApi'

export default function Changelog() {
  const { data: logs, loading, error, refetch } = useApi('/api/reading/changelogs.json')

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 48 }} role="status" aria-live="polite">
      <header>
        <h1 className="section-title" style={{ fontSize: 20 }}>Changelog</h1>
        <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
          Release history and version notes for QuickDash.
        </p>
      </header>

      <section aria-label="Release history">
        {loading ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {[1,2,3].map(i => (
              <div key={i} className="skeleton" style={{ width: '100%', height: 80 }} />
            ))}
          </div>
        ) : error ? (
          <div className="card" style={{ textAlign: 'center', padding: 32, borderColor: 'var(--md-error)' }}>
            <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-error)' }}>ERROR LOADING CHANGELOG</p>
            <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginTop: 8 }}>{error}</p>
            <button onClick={refetch} className="btn btn-sm" style={{ marginTop: 16 }}>RETRY</button>
          </div>
        ) : logs.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: 32 }}>
            <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)' }}>NO RELEASES YET</p>
            <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginTop: 8 }}>Changelog will appear here when releases are published.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {logs.map((l, i) => (
              <article key={i} className="card">
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 12, marginBottom: 8, flexWrap: 'wrap' }}>
                  <h2 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-primary)' }}>
                    v{l.version}
                  </h2>
                  {l.date && (
                    <span style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-on-surface-variant)' }}>
                      {new Date(l.date).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                    </span>
                  )}
                  {l.notes && (
                    <span className="tag" style={{ fontSize: 7 }}>{l.notes}</span>
                  )}
                </div>
                {Array.isArray(l.highlights) ? (
                  <ul style={{ paddingLeft: 20, fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.8 }}>
                    {l.highlights.map((c, j) => <li key={j}>{c}</li>)}
                  </ul>
                ) : (
                  <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)' }}>{l.highlights}</p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
