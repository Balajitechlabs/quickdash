import { testimonials } from '../data/homeData'
import FadeInSection from '../components/FadeInSection'

export default function TestimonialCarousel() {
  return (
    <FadeInSection as="section" aria-label="User testimonials">
      <h2 className="section-title">Testimonials</h2>
      <div className="grid-2">
        {testimonials.map((t, i) => (
          <div key={i} className="card" style={{ position: 'relative' }}>
            <span style={{ position: 'absolute', top: 12, left: 16, fontSize: 32, opacity: 0.15, fontFamily: 'serif' }}>"</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <div style={{
                width: 40, height: 40, borderRadius: '50%', background: 'var(--md-primary)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: 'var(--pixel-font)', fontSize: 14, color: 'var(--md-on-primary)',
                flexShrink: 0,
              }}>
                {t.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div>
                <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 8, color: 'var(--md-primary)' }}>{t.name}</p>
                <p style={{ fontSize: 11, color: 'var(--md-on-surface-variant)' }}>{t.role}</p>
              </div>
            </div>
            <p style={{ fontSize: 14, color: 'var(--md-on-surface-variant)', lineHeight: 1.7, fontStyle: 'italic', marginBottom: 12 }}>{t.text}</p>
            {t.device && (
              <p style={{ fontSize: 11, color: 'var(--md-primary)', opacity: 0.6 }}>Tested on {t.device}</p>
            )}
          </div>
        ))}
      </div>
    </FadeInSection>
  )
}
