import { Component } from 'react'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { error: null }
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary caught:', error, info)
  }

  render() {
    if (this.state.error) {
      return (
        <div className="container" style={{ textAlign: 'center', paddingTop: 64, paddingBottom: 64 }}>
          <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 48, color: 'var(--md-error)', marginBottom: 8 }}>!</p>
          <p style={{ fontFamily: 'var(--pixel-font)', fontSize: 10, color: 'var(--md-on-surface-variant)', marginBottom: 32 }}>
            SOMETHING WENT WRONG
          </p>
          <p style={{ color: 'var(--md-on-surface-variant)', fontSize: 14, marginBottom: 32 }}>
            {this.state.error.message}
          </p>
          <button onClick={() => window.location.reload()} className="btn btn-sm">
            RELOAD PAGE
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
