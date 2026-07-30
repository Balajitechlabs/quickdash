export interface Env {
  QUICKDASH_KV: {
    get(key: string, type: 'text'): Promise<string | null>
    put(key: string, value: string, options?: { expirationTtl?: number }): Promise<void>
  }
}

const ORIGIN = 'https://balajitechlabs.github.io'
const ALLOWED_ORIGIN = 'https://quickdash.balajitechlab.com'
const CORS: Record<string, string> = {
  'content-type': 'application/json',
  'access-control-allow-origin': ALLOWED_ORIGIN,
  'access-control-allow-methods': 'GET, POST, OPTIONS',
  'access-control-allow-headers': 'Content-Type',
}
const RATE_LIMIT = 3
const RATE_WINDOW = 60

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

    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: CORS })
    }

    if (path === '/api/v1/stats' || path === '/api/v1/stats.json') {
      return handleStats(env)
    }

    if (path === '/api/v1/feedback' && request.method === 'POST') {
      const ip = request.headers.get('cf-connecting-ip') || 'unknown'
      const allowed = await checkRateLimit(ip, env)
      if (!allowed) {
        return new Response(JSON.stringify({ status: 'error', message: 'Too many requests. Try again later.' }), { status: 429, headers: CORS })
      }
      return handleFeedback(request, env)
    }

    if (path === '/api/v1/crash-report' && request.method === 'POST') {
      const ip = request.headers.get('cf-connecting-ip') || 'unknown'
      const allowed = await checkRateLimit(ip, env)
      if (!allowed) {
        return new Response(JSON.stringify({ status: 'error', message: 'Too many requests. Try again later.' }), { status: 429, headers: CORS })
      }
      return handleCrashReport(request, env)
    }

    const originReq = new Request(ORIGIN + path + url.search, request)
    return fetch(originReq)
  }
}

async function handleStats(env: Env): Promise<Response> {
  try {
    const cached = await env.QUICKDASH_KV.get('stats:cached', 'text')
    if (cached) return new Response(cached, { headers: { ...CORS, 'cf-cache-status': 'HIT' } })

    const [releasesRes, toolsRes] = await Promise.allSettled([
      fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases?per_page=5', {
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

    const body = { downloads, tools, active_users: null, note: 'GA4 active users requires a Google Cloud service account' }
    const json = JSON.stringify(body)
    await env.QUICKDASH_KV.put('stats:cached', json, { expirationTtl: 60 })

    return new Response(json, { headers: { ...CORS, 'cf-cache-status': 'MISS' } })
  } catch (e: any) {
    return new Response(JSON.stringify({ error: e.message }), { status: 500, headers: CORS })
  }
}

async function handleFeedback(request: Request, env: Env): Promise<Response> {
  try {
    const body: any = await request.json()
    const entry = { ...body, received_at: new Date().toISOString() }
    const key = `feedback:${Date.now()}:${crypto.randomUUID()}`
    await env.QUICKDASH_KV.put(key, JSON.stringify(entry))
    await env.QUICKDASH_KV.put('feedback:latest', JSON.stringify(entry))
    return new Response(JSON.stringify({ status: 'ok' }), { headers: CORS })
  } catch (e: any) {
    return new Response(JSON.stringify({ status: 'error', message: e.message }), { status: 400, headers: CORS })
  }
}

async function handleCrashReport(request: Request, env: Env): Promise<Response> {
  try {
    const body: any = await request.json()
    const entry = { ...body, received_at: new Date().toISOString() }
    const key = `crash:${Date.now()}:${crypto.randomUUID()}`
    await env.QUICKDASH_KV.put(key, JSON.stringify(entry))
    await env.QUICKDASH_KV.put('crash:latest', JSON.stringify(entry))
    return new Response(JSON.stringify({ status: 'ok' }), { headers: CORS })
  } catch (e: any) {
    return new Response(JSON.stringify({ status: 'error', message: e.message }), { status: 400, headers: CORS })
  }
}
