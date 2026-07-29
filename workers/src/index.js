const ORIGIN = 'https://balajitechlabs.github.io'

export default {
  async fetch(request, env) {
    const url = new URL(request.url)
    const path = url.pathname

    if (path === '/api/v1/stats') {
      return handleStats(request, env)
    }

    if (path === '/api/v1/feedback' && request.method === 'POST') {
      return handleFeedback(request, env)
    }

    if (path === '/api/v1/crash-report' && request.method === 'POST') {
      return handleCrashReport(request, env)
    }

    const originReq = new Request(ORIGIN + path + url.search, request)
    return fetch(originReq)
  }
}

async function handleStats(request, env) {
  const cors = { 'content-type': 'application/json', 'access-control-allow-origin': '*' }

  try {
    const [releasesRes, toolsRes] = await Promise.allSettled([
      fetch('https://api.github.com/repos/Balajitechlabs/quickdash/releases?per_page=5', {
        headers: { 'User-Agent': 'QuickDash-Worker', 'Accept': 'application/vnd.github.v3+json' }
      }),
      fetch('https://quickdash.balajitechlab.com/api/v1/tools.json')
    ])

    let downloads = 0
    if (releasesRes.status === 'fulfilled' && releasesRes.value.ok) {
      const releases = await releasesRes.value.json()
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

    const body = {
      downloads,
      tools,
      active_users: null,
      note: 'GA4 active users requires a Google Cloud service account — set it up to enable live user count'
    }

    return new Response(JSON.stringify(body), { headers: cors })
  } catch (e) {
    return new Response(JSON.stringify({ error: e.message }), {
      status: 500, headers: cors
    })
  }
}

async function handleFeedback(request, env) {
  try {
    const body = await request.json()
    const entry = { ...body, received_at: new Date().toISOString() }
    const key = `feedback:${Date.now()}:${crypto.randomUUID()}`
    await env.QUICKDASH_KV.put(key, JSON.stringify(entry))
    await env.QUICKDASH_KV.put('feedback:latest', JSON.stringify(entry))
    return new Response(JSON.stringify({ status: 'ok' }), {
      headers: { 'content-type': 'application/json', 'access-control-allow-origin': '*' }
    })
  } catch (e) {
    return new Response(JSON.stringify({ status: 'error', message: e.message }), {
      status: 400, headers: { 'content-type': 'application/json', 'access-control-allow-origin': '*' }
    })
  }
}

async function handleCrashReport(request, env) {
  try {
    const body = await request.json()
    const entry = { ...body, received_at: new Date().toISOString() }
    const key = `crash:${Date.now()}:${crypto.randomUUID()}`
    await env.QUICKDASH_KV.put(key, JSON.stringify(entry))
    await env.QUICKDASH_KV.put('crash:latest', JSON.stringify(entry))
    return new Response(JSON.stringify({ status: 'ok' }), {
      headers: { 'content-type': 'application/json', 'access-control-allow-origin': '*' }
    })
  } catch (e) {
    return new Response(JSON.stringify({ status: 'error', message: e.message }), {
      status: 400, headers: { 'content-type': 'application/json', 'access-control-allow-origin': '*' }
    })
  }
}
