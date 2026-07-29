import { useState, useEffect } from 'react'

export default function Docs() {
  const [sections, setSections] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      let res = await fetch('/api/reading/docs')
      if (!res.ok) {
        res = await fetch('/api/reading/docs.json')
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()
      setSections(data)
      setLoading(false)
    } catch (e) {
      setError(e.message)
      setLoading(false)
    }
  }

  useEffect(load, [])

  const grouped = sections.reduce((acc, s) => {
    (acc[s.section] = acc[s.section] || []).push(s)
    return acc
  }, {})

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 48 }}>
      <header>
        <h1 className="section-title" style={{ fontSize: 20 }}>Documentation</h1>
        <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
          Setup guides, permission requirements, and developer documentation for QuickDash.
        </p>
      </header>

      <section aria-label="Documentation content">
        {loading ? (
          <div className="skeleton" style={{ width: '100%', height: 300 }} />
        ) : error ? (
          <div className="card" style={{ textAlign: 'center', padding: 32, borderColor: 'var(--md-error)' }}>
            <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-error)' }}>ERROR LOADING DOCS</p>
            <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginTop: 8 }}>{error}</p>
            <button onClick={load} className="btn btn-sm" style={{ marginTop: 16 }}>RETRY</button>
          </div>
        ) : sections.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: 32 }}>
            <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)' }}>NO DOCUMENTATION AVAILABLE</p>
            <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginTop: 8 }}>Docs are being written. Check the GitHub repository for developer guides.</p>
          </div>
        ) : (
          Object.entries(grouped).map(([groupName, items]) => (
            <div key={groupName} style={{ marginBottom: 32 }}>
              <h2 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-on-surface-variant)', opacity: 0.6, marginBottom: 12 }}>
                {groupName}
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {items.map(s => (
                  <article key={s.id} className="card">
                    <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 11, color: 'var(--md-primary)', marginBottom: 8 }}>
                      {s.title}
                    </h3>
                    <div style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.7, whiteSpace: 'pre-wrap' }}>
                      {s.content}
                    </div>
                  </article>
                ))}
              </div>
            </div>
          ))
        )}
      </section>
    </div>
  )
}
