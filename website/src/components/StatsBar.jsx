import { useState, useEffect } from 'react'

function formatNum(n) {
  if (n == null) return '...'
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M+'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K+'
  return String(n)
}

export default function StatsBar() {
  const [stats, setStats] = useState({ downloads: null, tools: 12, rating: '5.0 ★' })

  useEffect(() => {
    // Fetch 100% REAL download stats from GitHub Releases API
    fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases?per_page=100')
      .then(r => r.ok ? r.json() : [])
      .then(releases => {
        if (Array.isArray(releases)) {
          let totalDownloads = 0
          releases.forEach(rel => {
            (rel.assets || []).forEach(asset => {
              totalDownloads += asset.download_count || 0
            })
          })
          setStats(prev => ({ ...prev, downloads: totalDownloads }))
        }
      })
      .catch(err => {
        console.error('Failed to fetch GitHub release stats:', err)
      })
  }, [])

  const items = [
    { label: 'REAL DOWNLOADS', value: stats.downloads !== null ? formatNum(stats.downloads) : '...' },
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
