import { features } from '../data/homeData'
import FadeInSection from '../components/FadeInSection'

export default function FeatureGrid() {
  return (
    <FadeInSection as="section" aria-labelledby="features-title">
      <h2 id="features-title" className="section-title">Features</h2>
      <div className="grid-3">
        {features.map(f => (
          <div key={f.id} className="card" style={{ textAlign: 'center' }}>
            <span role="img" aria-label={`${f.name} icon`} className="feature-icon" style={{ fontSize: 32, display: 'block', marginBottom: 8 }}>{f.icon}</span>
            <h3 style={{ fontFamily: 'var(--pixel-font)', fontSize: 9, color: 'var(--md-primary)', marginBottom: 8 }}>{f.name}{['translator','qr','color','text'].includes(f.id) && <span className="demo-badge">DEMO</span>}</h3>
            <p style={{ fontSize: 13, color: 'var(--md-on-surface-variant)' }}>{f.desc}</p>
          </div>
        ))}
      </div>
    </FadeInSection>
  )
}
