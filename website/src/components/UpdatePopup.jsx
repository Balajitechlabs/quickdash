import { useState, useEffect, useRef } from 'react'

export default function UpdatePopup() {
  const [data, setData] = useState(null)
  const [dismissed, setDismissed] = useState(() => {
    return localStorage.getItem('qd-update-dismissed')
  })
  const [visible, setVisible] = useState(false)
  const timerRef = useRef(null)

  useEffect(() => {
    fetch('/api/v1/update.json')
      .then(r => r.json())
      .then(d => setData(d))
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!dismissed && data) {
      timerRef.current = setTimeout(() => setVisible(true), 2000)
    }
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current)
    }
  }, [dismissed, data])

  useEffect(() => {
    if (!visible) return
    const handler = (e) => { if (e.key === 'Escape') handleDismiss() }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [visible])

  const handleDismiss = () => {
    setVisible(false)
    setDismissed(true)
    if (data) {
      localStorage.setItem('qd-update-dismissed', data.id)
    }
  }

  if (!visible || !data) return null

  return (
    <>
      <div onClick={handleDismiss} style={{
        position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 998,
        animation: 'fadeIn 0.2s ease',
      }} />
      <div style={{
        position: 'fixed', bottom: 24, right: 24, maxWidth: 360, width: '100%', zIndex: 999,
        animation: 'slideUp 0.3s ease',
      }}>
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <div style={{
            background: data.critical ? 'var(--md-error)' : 'var(--md-primary)',
            color: data.critical ? 'var(--md-on-error)' : 'var(--md-on-primary)',
            padding: '10px 16px', display: 'flex', alignItems: 'center',
            justifyContent: 'space-between',
            fontFamily: 'var(--pixel-font)', fontSize: 8, letterSpacing: 0.5,
            textTransform: 'uppercase',
          }}>
            <span>{data.critical ? 'Critical Update' : 'Update Available'}</span>
            <span style={{ fontSize: 7, opacity: 0.7 }}>v{data.version}</span>
          </div>
          <div style={{ padding: 16 }}>
            <h4 style={{
              fontFamily: 'var(--pixel-font)', fontSize: 10, marginBottom: 8,
              letterSpacing: 0.5, color: 'var(--md-primary)',
            }}>{data.title}</h4>
            <p style={{
              fontSize: 13, color: 'var(--md-on-surface-variant)', marginBottom: 16,
              lineHeight: 1.6, maxHeight: 80, overflow: 'hidden',
            }}>{data.summary}</p>
            <div style={{ display: 'flex', gap: 8 }}>
              {data.link && (
                <a href={data.link} target="_blank" rel="noopener noreferrer"
                   className="btn" style={{ fontSize: 8, padding: '8px 16px' }}>
                  Download
                </a>
              )}
              <button onClick={handleDismiss} className="btn btn-outline" style={{ fontSize: 8, padding: '8px 16px' }}>
                Dismiss
              </button>
            </div>
          </div>
        </div>
      </div>
      <style>{`
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
      `}</style>
    </>
  )
}
