import { useState, useEffect } from 'react'

function formatNum(n) {
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M+'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K+'
  return String(n)
}

export default function StatsBar() {
  const [stats, setStats] = useState({ downloads: null, tools: null, active_users: null })

  useEffect(() => {
    fetch('/api/v1/stats')
      .then(r => r.ok ? r.json() : null)
      .then(d => { if (d) setStats(d) })
      .catch(() => {})
  }, [])

  const items = [
    { label: 'DOWNLOADS', value: stats.downloads != null ? formatNum(stats.downloads) : '...' },
    { label: 'TOOLS', value: stats.tools != null ? String(stats.tools) : '...' },
    { label: 'ACTIVE USERS', value: stats.active_users != null ? formatNum(stats.active_users) : '...' },
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
