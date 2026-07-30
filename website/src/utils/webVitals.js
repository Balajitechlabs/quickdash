export function reportWebVitals() {
  if (typeof window === 'undefined' || !window.gtag) return

  try {
    new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        const name = entry.entryType === 'largest-contentful-paint' ? 'LCP'
          : entry.entryType === 'first-input' ? 'FID'
          : entry.entryType === 'layout-shift' ? 'CLS'
          : entry.name

        const value = entry.entryType === 'layout-shift'
          ? entry.value
          : entry.startTime || entry.processingStart

        window.gtag('event', 'web_vitals', {
          event_category: 'Web Vitals',
          event_label: name,
          value: Math.round(name === 'CLS' ? value * 1000 : value),
          non_interaction: true,
        })
      }
    }).observe({ type: 'largest-contentful-paint', buffered: true })

    new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        window.gtag('event', 'web_vitals', {
          event_category: 'Web Vitals',
          event_label: 'FID',
          value: Math.round(entry.processingStart - entry.startTime),
          non_interaction: true,
        })
      }
    }).observe({ type: 'first-input', buffered: true })

    new PerformanceObserver((list) => {
      let cls = 0
      for (const entry of list.getEntries()) {
        if (!entry.hadRecentInput) cls += entry.value
      }
      window.gtag('event', 'web_vitals', {
        event_category: 'Web Vitals',
        event_label: 'CLS',
        value: Math.round(cls * 1000),
        non_interaction: true,
      })
    }).observe({ type: 'layout-shift', buffered: true })
  } catch (e) {
    // PerformanceObserver not supported or blocked
  }
}
