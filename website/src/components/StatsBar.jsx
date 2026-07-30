import { useState, useEffect } from 'react'

function formatNum(n) {
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M+'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K+'
  return String(n)
}

export default function StatsBar() {
  const [stats, setStats] = useState({ downloads: 1250, tools: 12, active_users: 500 })

  useEffect(() => {
    fetch('/api/v1/stats')
      .then(r => r.ok ? r.json() : null)
      .then(d => {
        if (d && (d.downloads != null || d.tools != null)) {
          setStats(prev => ({
            downloads: d.downloads ?? prev.downloads,
            tools: d.tools ?? prev.tools,
            active_users: d.active_users ?? prev.active_users,
          }))
        }
      })
      .catch(() => {
        // Fallback to GitHub Releases API if worker endpoint is unavailable
        fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases')
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
    { label: 'DOWNLOADS', value: stats.downloads != null ? formatNum(stats.downloads) : '1.2K+' },
    { label: 'TOOLS', value: stats.tools != null ? String(stats.tools) : '12' },
    { label: 'ACTIVE USERS', value: stats.active_users != null ? formatNum(stats.active_users) : '500+' },
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
