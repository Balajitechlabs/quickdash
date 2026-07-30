export function trackEvent(action, label) {
  if (typeof gtag !== 'undefined') {
    gtag('event', action, { event_label: label })
  }
}
