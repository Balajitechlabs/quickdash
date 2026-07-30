import { useState } from 'react'
import { features } from '../data/homeData'
import FadeInSection from '../components/FadeInSection'

export default function FeatureGrid() {
  const [query, setQuery] = useState('')
  const [selectedTool, setSelectedTool] = useState(null)

  const filtered = features.filter(f =>
    f.name.toLowerCase().includes(query.toLowerCase()) ||
    f.desc.toLowerCase().includes(query.toLowerCase())
  )

  return (
    <FadeInSection as="section" aria-labelledby="features-title">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, marginBottom: 24 }}>
        <h2 id="features-title" className="section-title" style={{ margin: 0 }}>12 Floating Tools</h2>
        <input
          type="text"
          placeholder="Search tools (e.g. QR, Notes, Translator)..."
          value={query}
          onChange={e => setQuery(e.target.value)}
          aria-label="Filter floating tools"
          style={{
            padding: '8px 16px', borderRadius: 20, border: '1px solid var(--md-outline)',
            background: 'var(--md-surface-1)', color: 'var(--md-on-surface)',
            fontSize: 13, minWidth: 260, outline: 'none'
          }}
        />
      </div>

      <div className="grid-3">
        {filtered.length === 0 && (
          <div className="card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: 32 }}>
            <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)' }}>NO TOOLS MATCHING "{query.toUpperCase()}"</p>
          </div>
        )}
        {filtered.map(f => (
          <div
            key={f.id}
            className="card"
            style={{ textAlign: 'center', cursor: 'pointer', transition: 'transform 0.2s ease, border-color 0.2s ease' }}
            onClick={() => setSelectedTool(f)}
          >
            <span role="img" aria-label={`${f.name} icon`} className="feature-icon" style={{ fontSize: 36, display: 'block', marginBottom: 12 }}>{f.icon}</span>
            <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-primary)', marginBottom: 8 }}>{f.name}</h3>
            <p style={{ fontSize: 13, color: 'var(--md-on-surface-variant)', lineHeight: 1.5 }}>{f.desc}</p>
            <span style={{ display: 'inline-block', marginTop: 12, fontSize: 10, fontFamily: 'var(--pixel-font)', color: 'var(--md-primary)', opacity: 0.8 }}>PREVIEW TOOL →</span>
          </div>
        ))}
      </div>

      {selectedTool && (
        <div
          role="dialog"
          aria-modal="true"
          onClick={() => setSelectedTool(null)}
          style={{
            position: 'fixed', inset: 0, zIndex: 1000,
            background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 16
          }}
        >
          <div
            onClick={e => e.stopPropagation()}
            className="card"
            style={{
              maxWidth: 420, width: '100%', background: 'var(--md-surface)',
              border: '2px solid var(--md-primary)', borderRadius: 24, padding: 24,
              boxShadow: '0 8px 32px rgba(0,0,0,0.3)', animation: 'slideUp 0.25s ease'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <span style={{ fontSize: 28 }}>{selectedTool.icon}</span>
                <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 12, color: 'var(--md-primary)', margin: 0 }}>{selectedTool.name}</h3>
              </div>
              <button onClick={() => setSelectedTool(null)} style={{ background: 'none', border: 'none', fontSize: 18, color: 'var(--md-on-surface-variant)', cursor: 'pointer' }}>✕</button>
            </div>
            <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.6, marginBottom: 20 }}>{selectedTool.desc}</p>
            
            <div style={{ background: 'var(--md-surface-1)', borderRadius: 16, padding: 16, textAlign: 'center', border: '1px dashed var(--md-outline)' }}>
              <div style={{ fontSize: 11, fontFamily: 'var(--pixel-font)', color: 'var(--md-primary)', marginBottom: 6 }}>ON-DEVICE FLOATING WINDOW</div>
              <p style={{ fontSize: 12, color: 'var(--md-on-surface-variant)', margin: 0 }}>Operates over any app seamlessly with zero tracking & full Material 3 customizability.</p>
            </div>
            
            <button className="btn" style={{ width: '100%', marginTop: 20 }} onClick={() => setSelectedTool(null)}>
              CLOSE PREVIEW
            </button>
          </div>
        </div>
      )}
    </FadeInSection>
  )
}
