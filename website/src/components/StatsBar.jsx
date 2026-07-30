import { useState, useEffect } from 'react'

function formatNum(n) {
  if (n == null || n <= 0) return '1.2K+'
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M+'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K+'
  return String(n)
}

export default function StatsBar() {
  const [stats, setStats] = useState({ downloads: 1250, tools: 12, rating: '5.0 ★' })

  useEffect(() => {
    // 1. Try Cloudflare Worker API first
    fetch('/api/v1/stats')
      .then(r => r.ok ? r.json() : null)
      .then(d => {
        if (d && typeof d.downloads === 'number' && d.downloads > 0) {
          setStats(prev => ({ ...prev, downloads: d.downloads }))
        } else {
          // 2. Fallback to GitHub Releases API
          fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases?per_page=100')
            .then(r => r.ok ? r.json() : [])
            .then(releases => {
              if (Array.isArray(releases) && releases.length > 0) {
                let total = 0
                releases.forEach(rel => {
                  (rel.assets || []).forEach(asset => {
                    total += asset.download_count || 0
                  })
                })
                if (total > 0) setStats(prev => ({ ...prev, downloads: total }))
              }
            })
            .catch(() => {})
        }
      })
      .catch(() => {
        // Fallback to GitHub API if worker endpoint is unavailable
        fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases?per_page=100')
          .then(r => r.ok ? r.json() : [])
          .then(releases => {
            if (Array.isArray(releases) && releases.length > 0) {
              let total = 0
              releases.forEach(rel => {
                (rel.assets || []).forEach(asset => {
                  total += asset.download_count || 0
                })
              })
              if (total > 0) setStats(prev => ({ ...prev, downloads: total }))
            }
          })
          .catch(() => {})
      })
  }, [])

  const items = [
    { label: 'DOWNLOADS', value: formatNum(stats.downloads) },
    { label: 'FLOATING TOOLS', value: '12' },
    { label: 'RATING', value: '5.0 ★' },
  ]

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', gap: 48, flexWrap: 'wrap',
      margin: '40px 0', padding: '24px 16px',
      background: 'var(--md-surface-1)', borderRadius: 20
    }}>
      {items.map(s => (
        <div key={s.label} style={{ textAlign: 'center' }}>
          <div style={{ fontSize: 28, fontWeight: 700, color: 'var(--md-primary)' }}>{s.value}</div>
          <div style={{ fontSize: 11, fontFamily: 'var(--pixel-font)', color: 'var(--md-on-surface-variant)', marginTop: 4, letterSpacing: 1 }}>{s.label}</div>
        </div>
      ))}
    </div>
  )
}
