import { useState, useMemo } from 'react'
import FadeInSection from '../components/FadeInSection'
import Screenshot from '../components/Screenshot'
import Lightbox from '../components/Lightbox'
import { trackEvent } from '../utils/analytics'

export default function GallerySection() {
  const [lightbox, setLightbox] = useState(null)
  const total = 15
  const images = useMemo(() => Array.from({ length: total }, (_, i) => ({
    src: `/assets/gallery/shot_${i + 1}.png`,
    alt: `QuickDash screenshot ${i + 1} — feature overview`,
    label: `screenshot_${i + 1}`,
  })), [])

  return (
    <FadeInSection as="section" aria-label="QuickDash screenshots gallery">
      <h2 className="section-title">Gallery</h2>
      <div style={{ display: 'flex', gap: 12, overflowX: 'auto', paddingBottom: 12, scrollSnapType: 'x mandatory', WebkitOverflowScrolling: 'touch' }}>
        {images.map((img, i) => (
          <div key={i} style={{ flex: '0 0 auto', scrollSnapAlign: 'start' }}>
            <Screenshot src={img.src} alt={img.alt} priority={i < 3} onOpen={() => { trackEvent('gallery_view', img.label); setLightbox(img.src); }} />
          </div>
        ))}
      </div>
      {lightbox && <Lightbox src={lightbox} onClose={() => setLightbox(null)} />}
    </FadeInSection>
  )
}
