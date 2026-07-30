import { useEffect } from 'react'

export default function Lightbox({ src, onClose }) {
  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', handleKey)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKey)
      document.body.style.overflow = ''
    }
  }, [onClose])

  return (
    <div
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Image fullscreen view"
      style={{
        position: 'fixed', inset: 0, zIndex: 9999,
        background: 'rgba(0,0,0,0.85)', display: 'flex',
        alignItems: 'center', justifyContent: 'center',
        cursor: 'zoom-out', animation: 'fadeIn 0.2s ease',
        padding: 20,
      }}
    >
      <img
        src={src}
        alt="Full size screenshot"
        style={{ maxWidth: '100%', maxHeight: '90vh', borderRadius: 8, boxShadow: '0 8px 40px rgba(0,0,0,0.5)' }}
      />
      <button
        onClick={onClose}
        aria-label="Close lightbox"
        style={{
          position: 'absolute', top: 20, right: 20,
          background: 'none', border: 'none', color: 'white',
          fontSize: 32, cursor: 'pointer', fontFamily: 'var(--body-font)',
          opacity: 0.8, width: 48, height: 48, display: 'flex',
          alignItems: 'center', justifyContent: 'center',
          borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.1)',
        }}
      >
        ✕
      </button>
    </div>
  )
}
