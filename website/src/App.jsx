import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Layout from './components/Layout'

const Home = lazy(() => import('./pages/Home'))
const Blog = lazy(() => import('./pages/Blog'))
const Docs = lazy(() => import('./pages/Docs'))
const Changelog = lazy(() => import('./pages/Changelog'))
const Privacy = lazy(() => import('./pages/Privacy'))

function LoadingPage() {
  return (
    <div className="container" style={{ paddingTop: 48, paddingBottom: 48 }}>
      <div className="skeleton" style={{ width: '50%', height: 28, marginBottom: 24, margin: '0 auto 24px' }} />
      <div className="skeleton" style={{ width: '100%', height: 200, marginBottom: 16 }} />
      <div className="skeleton" style={{ width: '100%', height: 160, marginBottom: 16 }} />
      <div className="skeleton" style={{ width: '100%', height: 160 }} />
    </div>
  )
}

function NotFound() {
  return (
    <div className="container" style={{ textAlign: 'center', paddingTop: 64, paddingBottom: 64 }}>
      <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 64, color: 'var(--md-primary)', marginBottom: 8 }}>404</p>
      <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)', marginBottom: 32 }}>
        PAGE NOT FOUND
      </p>
      <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
        The page you are looking for does not exist or has been moved.
      </p>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap' }}>
        <Link to="/" className="btn">Back to Home</Link>
        <Link to="/reading" className="btn btn-outline">Reading</Link>
        <Link to="/docs" className="btn btn-outline">Docs</Link>
        <Link to="/changelog" className="btn btn-outline">Changelog</Link>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Suspense fallback={<LoadingPage />}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/reading" element={<Blog />} />
            <Route path="/docs" element={<Docs />} />
            <Route path="/changelog" element={<Changelog />} />
            <Route path="/privacy" element={<Privacy />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </Layout>
    </BrowserRouter>
  )
}
