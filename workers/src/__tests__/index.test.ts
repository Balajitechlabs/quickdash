import { SELF } from 'cloudflare:test'
import { describe, it, expect } from 'vitest'

const BASE = 'https://quickdash.balajitechlab.com'

describe('QuickDash API Worker', () => {
  it('GET /api/v1/stats returns 200 with expected JSON fields', async () => {
    const res = await SELF.fetch(`${BASE}/api/v1/stats`)
    expect(res.status).toBe(200)
    expect(res.headers.get('content-type')).toMatch(/json/)
    const body = await res.json() as Record<string, unknown>
    expect(body).toHaveProperty('downloads')
    expect(body).toHaveProperty('tools')
    expect(body).toHaveProperty('active_users')
    expect(body).toHaveProperty('note')
  })

  it('GET /api/v1/stats.json returns 200', async () => {
    const res = await SELF.fetch(`${BASE}/api/v1/stats.json`)
    expect(res.status).toBe(200)
  })

  it('includes CORS headers on all responses', async () => {
    const res = await SELF.fetch(`${BASE}/api/v1/stats`)
    expect(res.headers.get('access-control-allow-origin')).toBe('https://quickdash.balajitechlab.com')
    expect(res.headers.get('access-control-allow-methods')).toBe('GET, POST, OPTIONS')
    expect(res.headers.get('access-control-allow-headers')).toBe('Content-Type')
  })

  it('OPTIONS preflight returns 204 with CORS headers', async () => {
    const res = await SELF.fetch(`${BASE}/api/v1/stats`, { method: 'OPTIONS' })
    expect(res.status).toBe(204)
    expect(res.headers.get('access-control-allow-origin')).toBe('https://quickdash.balajitechlab.com')
    expect(res.headers.get('access-control-allow-methods')).toBe('GET, POST, OPTIONS')
    expect(res.headers.get('access-control-allow-headers')).toBe('Content-Type')
  })

  it('returns 404 for unknown routes', async () => {
    const res = await SELF.fetch(`${BASE}/api/v1/unknown`)
    expect(res.status).toBe(404)
  })

  it('rate limits POST /api/v1/feedback — 3 requests allowed, 4th returns 429', async () => {
    const ip = '10.0.0.1'
    const body = JSON.stringify({ message: 'rate limit test' })
    const headers = { 'Content-Type': 'application/json', 'cf-connecting-ip': ip }

    for (let i = 0; i < 3; i++) {
      const res = await SELF.fetch(`${BASE}/api/v1/feedback`, {
        method: 'POST',
        headers,
        body,
      })
      expect(res.status).toBe(200)
    }

    const res = await SELF.fetch(`${BASE}/api/v1/feedback`, {
      method: 'POST',
      headers,
      body,
    })
    expect(res.status).toBe(429)
    const data = await res.json() as Record<string, unknown>
    expect(data.status).toBe('error')
    expect(data.message).toMatch(/Too many requests/)
  })

  it('POST /api/v1/feedback with valid JSON body succeeds', async () => {
    const body = JSON.stringify({ message: 'Great app!', email: 'test@example.com', rating: 5 })
    const res = await SELF.fetch(`${BASE}/api/v1/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'cf-connecting-ip': '10.0.0.2' },
      body,
    })
    expect(res.status).toBe(200)
    const data = await res.json() as Record<string, unknown>
    expect(data.status).toBe('ok')
  })
})
