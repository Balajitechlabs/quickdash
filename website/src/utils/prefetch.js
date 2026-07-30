const prefetched = new Set()

export function prefetchPage(path) {
  if (prefetched.has(path)) return
  prefetched.add(path)

  const chunks = {
    '/': () => import('../pages/Home'),
    '/reading': () => import('../pages/Blog'),
    '/docs': () => import('../pages/Docs'),
    '/changelog': () => import('../pages/Changelog'),
    '/privacy': () => import('../pages/Privacy'),
  }

  const loader = chunks[path]
  if (loader) {
    loader().catch(() => {})
  }
}

export function prefetchAllOnIdle() {
  if ('requestIdleCallback' in window) {
    requestIdleCallback(() => {
      const paths = ['/', '/reading', '/docs', '/changelog', '/privacy']
      for (const p of paths) prefetchPage(p)
    }, { timeout: 2000 })
  }
}
