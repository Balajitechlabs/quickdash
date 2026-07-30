const CACHE = 'quickdash-v9'
const PRECACHE = [
  '/', '/privacy', '/reading', '/docs', '/changelog',
  '/assets/logo.png',
  '/api/v1/update.json', '/api/v1/health.json', '/api/v1/tools.json',
]
self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(c => c.addAll(PRECACHE)).then(() => self.skipWaiting())
  )
})
self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
    .then(() => self.clients.claim())
  )
})
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return
  e.respondWith(
    caches.match(e.request).then(cached => {
      const fetchPromise = fetch(e.request).then(net => {
        if (net.ok) {
          const clone = net.clone()
          caches.open(CACHE).then(c => c.put(e.request, clone))
        }
        return net
      }).catch(() => cached)
      return cached || fetchPromise
    })
  )
})
