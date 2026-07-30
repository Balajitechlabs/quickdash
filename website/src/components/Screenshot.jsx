import { useState } from 'react'

export default function Screenshot({ src, alt, priority, onOpen }) {
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState(false)

  if (error) {
    return (
      <div className="card" style={{ padding: 16, textAlign: 'center', aspectRatio: '9/19', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}>
        <span style={{ fontSize: 32 }}>📱</span>
        <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 7, color: 'var(--md-on-surface-variant)' }}>LOAD FAILED</p>
      </div>
    )
  }

  return (
    <div className="card" style={{ padding: 4, cursor: 'pointer', opacity: loaded ? 1 : 0.5 }} onClick={() => onOpen(src)} role="button" tabIndex={0} onKeyDown={(e) => e.key === 'Enter' && onOpen(src)} aria-label="Open screenshot full-size">
      {!loaded && <div className="skeleton" style={{ width: '100%', aspectRatio: '9/19' }} />}
      <picture>
        <source srcSet={src.replace('.png', '.webp')} type="image/webp" />
        <img
          src={src}
          alt={alt}
          loading={priority ? 'eager' : 'lazy'}
          fetchpriority={priority ? 'high' : undefined}
          width="200"
          height="420"
          onLoad={() => setLoaded(true)}
          onError={() => setError(true)}
          style={{ display: 'block', width: '160px', borderRadius: 4 }}
        />
      </picture>
    </div>
  )
}
