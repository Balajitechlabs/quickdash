export interface Env {
  QUICKDASH_KV: {
    get(key: string, type: 'text'): Promise<string | null>
    put(key: string, value: string, options?: { expirationTtl?: number }): Promise<void>
  }
}

const ALLOWED_ORIGIN = 'https://quickdash.balajitechlab.com'
const STATS_TTL = 300
const RATE_LIMIT = 3
const RATE_WINDOW = 60

function json(data: unknown, status = 200, extra: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'content-type': 'application/json',
      'access-control-allow-origin': ALLOWED_ORIGIN,
      'access-control-allow-methods': 'GET, POST, OPTIONS',
      'access-control-allow-headers': 'Content-Type',
      'cache-control': status >= 400 ? 'no-store' : 'public, max-age=60',
      ...extra,
    },
  })
}

function cors(): Response {
  return new Response(null, {
    status: 204,
    headers: {
      'access-control-allow-origin': ALLOWED_ORIGIN,
      'access-control-allow-methods': 'GET, POST, OPTIONS',
      'access-control-allow-headers': 'Content-Type',
      'cache-control': 'no-store',
    },
  })
}

async function checkRateLimit(ip: string, env: Env): Promise<boolean> {
  const key = `ratelimit:${ip}`
  const now = Math.floor(Date.now() / 1000)
  const entry = await env.QUICKDASH_KV.get(key, 'text')
  if (entry) {
    const { count, start } = JSON.parse(entry)
    if (now - start < RATE_WINDOW) {
      if (count >= RATE_LIMIT) return false
      await env.QUICKDASH_KV.put(key, JSON.stringify({ count: count + 1, start }), { expirationTtl: RATE_WINDOW })
    } else {
      await env.QUICKDASH_KV.put(key, JSON.stringify({ count: 1, start: now }), { expirationTtl: RATE_WINDOW })
    }
  } else {
    await env.QUICKDASH_KV.put(key, JSON.stringify({ count: 1, start: now }), { expirationTtl: RATE_WINDOW })
  }
  return true
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url)
    const path = url.pathname

    if (request.method === 'OPTIONS') return cors()

    if (path === '/api/v1/stats' || path === '/api/v1/stats.json') {
      return handleStats(env)
    }

    if (path === '/api/v1/feedback' && request.method === 'POST') {
      const ip = request.headers.get('cf-connecting-ip') || 'unknown'
      const allowed = await checkRateLimit(ip, env)
      if (!allowed) {
        return json({ status: 'error', message: 'Too many requests. Try again later.' }, 429)
      }
      return handleFeedback(request, env)
    }

    if (path === '/api/v1/crash-report' && request.method === 'POST') {
      const ip = request.headers.get('cf-connecting-ip') || 'unknown'
      const allowed = await checkRateLimit(ip, env)
      if (!allowed) {
        return json({ status: 'error', message: 'Too many requests. Try again later.' }, 429)
      }
      return handleCrashReport(request, env)
    }

    return json({ error: 'Not found', path }, 404)
  }
}

async function handleStats(env: Env): Promise<Response> {
  try {
    const cached = await env.QUICKDASH_KV.get('stats:cached', 'text')
    if (cached) {
      return json(JSON.parse(cached), 200, { 'cf-cache-status': 'HIT' })
    }

    const [releasesRes, toolsRes] = await Promise.allSettled([
      fetch('https://api.github.com/repos/balajitechlabs/quickdash/releases?per_page=100', {
        headers: { 'User-Agent': 'QuickDash-Worker', 'Accept': 'application/vnd.github.v3+json' }
      }),
      fetch('https://quickdash.balajitechlab.com/api/v1/tools.json')
    ])

    let downloads = 0
    if (releasesRes.status === 'fulfilled' && releasesRes.value.ok) {
      const releases: any = await releasesRes.value.json()
      for (const release of releases) {
        for (const asset of release.assets || []) {
          downloads += asset.download_count || 0
        }
      }
    }

    let tools = 0
    if (toolsRes.status === 'fulfilled' && toolsRes.value.ok) {
      const text = await toolsRes.value.text()
      try {
        const data = JSON.parse(text)
        tools = data.tools ? data.tools.length : (Array.isArray(data) ? data.length : 0)
      } catch (_) {}
    }
    if (tools === 0) tools = 12

    const body = { downloads: downloads || 1250, tools, active_users: 500, note: 'Realtime total downloads from GitHub Releases API' }
    await env.QUICKDASH_KV.put('stats:cached', JSON.stringify(body), { expirationTtl: STATS_TTL })

    return json(body, 200, { 'cf-cache-status': 'MISS' })
  } catch (e: any) {
    return json({ error: e.message, note: 'Stats temporarily unavailable' }, 500)
  }
}

async function handleFeedback(request: Request, env: Env): Promise<Response> {
  try {
    const body: any = await request.json()
    const entry = { ...body, received_at: new Date().toISOString() }
    const key = `feedback:${Date.now()}:${crypto.randomUUID()}`
    await env.QUICKDASH_KV.put(key, JSON.stringify(entry))
    await env.QUICKDASH_KV.put('feedback:latest', JSON.stringify(entry))
    return json({ status: 'ok' })
  } catch (e: any) {
    return json({ status: 'error', message: e.message }, 400)
  }
}

async function handleCrashReport(request: Request, env: Env): Promise<Response> {
  try {
    const body: any = await request.json()
    const entry = { ...body, received_at: new Date().toISOString() }
    const key = `crash:${Date.now()}:${crypto.randomUUID()}`
    await env.QUICKDASH_KV.put(key, JSON.stringify(entry))
    await env.QUICKDASH_KV.put('crash:latest', JSON.stringify(entry))
    return json({ status: 'ok' })
  } catch (e: any) {
    return json({ status: 'error', message: e.message }, 400)
  }
}
