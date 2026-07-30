import { specs } from '../data/homeData'
import FadeInSection from '../components/FadeInSection'

export default function SpecsTable() {
  return (
    <FadeInSection as="section" aria-labelledby="specs-title">
      <h2 id="specs-title" className="section-title">Tech Specs</h2>
      <div className="card" style={{ overflow: 'hidden', padding: 0, borderRadius: 8 }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <caption className="sr-only">QuickDash technical specifications</caption>
          <tbody>
            {specs.map(s => (
              <tr key={s.label} style={{ borderBottom: '1px solid var(--md-outline)' }}>
                <td style={{ padding: '12px 20px', fontFamily: 'var(--pixel-font)', fontSize: 9, color: 'var(--md-primary)', background: 'var(--md-primary-container)', width: '40%', borderRight: '1px solid var(--md-outline)' }}>
                  {s.label}
                </td>
                <td style={{ padding: '12px 20px', fontFamily: 'var(--body-font)', fontSize: 14, color: 'var(--md-on-surface)' }}>
                  {s.value}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </FadeInSection>
  )
}
