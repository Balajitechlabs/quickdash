const ORIGIN = 'https://balajitechlabs.github.io'

export default {
  async fetch(request, env) {
    const url = new URL(request.url)
    const path = url.pathname

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
