import { useState, useCallback } from 'react'
import { themes } from '../data/homeData'
import FadeInSection from '../components/FadeInSection'
import { trackEvent } from '../utils/analytics'

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

export default function ThemeShowcase({ onPreview }) {
  const [activeTheme, setActiveTheme] = useState(null)

  const applyTheme = useCallback((colors, name) => {
    trackEvent('theme_preview', name)
    onPreview(colors)
    setActiveTheme(name)
    setTimeout(() => {
      onPreview(null)
      setActiveTheme(null)
    }, 3000)
  }, [onPreview])

  return (
    <FadeInSection as="section" aria-label="Live theme preview">
      <h2 className="section-title">Theme Presets</h2>
      <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 13, marginBottom: 16 }}>
        Click a preset to preview. Lasts 3 seconds.
      </p>
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
    </FadeInSection>
  )
}
