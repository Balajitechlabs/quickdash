import express from 'express'
import cors from 'cors'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import rateLimit from 'express-rate-limit'
import postsRouter from './modules/posts.js'
import changelogsRouter from './modules/changelogs.js'
import docsRouter from './modules/docs.js'
import updatesRouter from './modules/updates.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const app = express()
const PORT = process.env.PORT || 4000

// Rate limiting — prevents API abuse
const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per window
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again later.' },
})

const writeLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20, // stricter limit for write endpoints
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again later.' },
})

app.use(cors())
app.use(express.json({ limit: '10kb' })) // limit request body size

// Apply rate limiting to all API routes
app.use('/api/reading', apiLimiter)

app.use('/api/reading/posts', postsRouter)
app.use('/api/reading/changelogs', changelogsRouter)
app.use('/api/reading/docs', docsRouter)
app.use('/api/reading/updates', updatesRouter)

app.get('/api/reading/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() })
})

// Sanitize user input to prevent log injection
function sanitizeForLog(input) {
  if (typeof input === 'string') {
    return input.replace(/[\r\n\t]/g, ' ').substring(0, 500)
  }
  if (typeof input === 'object' && input !== null) {
    const sanitized = {}
    for (const [key, value] of Object.entries(input)) {
      const safeKey = String(key).replace(/[\r\n\t]/g, ' ').substring(0, 100)
      sanitized[safeKey] = sanitizeForLog(value)
    }
    return sanitized
  }
  return input
}

// Validate feedback/crash-report data before writing to file
function validateAndSanitizeEntry(body, maxFields = 10) {
  if (typeof body !== 'object' || body === null || Array.isArray(body)) {
    return null
  }
  const sanitized = {}
  const keys = Object.keys(body).slice(0, maxFields)
  for (const key of keys) {
    const safeKey = String(key).replace(/[^a-zA-Z0-9_-]/g, '').substring(0, 50)
    if (!safeKey) continue
    const value = body[key]
    if (typeof value === 'string') {
      sanitized[safeKey] = value.substring(0, 2000)
    } else if (typeof value === 'number' || typeof value === 'boolean') {
      sanitized[safeKey] = value
    }
  }
  sanitized.received_at = new Date().toISOString()
  return sanitized
}

app.post('/api/v1/feedback', writeLimiter, (req, res) => {
  const entry = validateAndSanitizeEntry(req.body)
  if (!entry) {
    return res.status(400).json({ error: 'Invalid feedback data' })
  }
  const line = JSON.stringify(entry) + '\n'
  const filePath = path.join(__dirname, 'data', 'feedback.log')
  fs.appendFile(filePath, line, (err) => {
    if (err) console.error('Failed to write feedback')
  })
  console.log('Feedback received:', sanitizeForLog(entry.id || 'anonymous'))
  res.json({ status: 'ok' })
})

app.post('/api/v1/crash-report', writeLimiter, (req, res) => {
  const entry = validateAndSanitizeEntry(req.body, 15)
  if (!entry) {
    return res.status(400).json({ error: 'Invalid crash report data' })
  }
  const line = JSON.stringify(entry) + '\n'
  const filePath = path.join(__dirname, 'data', 'crash-reports.log')
  fs.appendFile(filePath, line, (err) => {
    if (err) console.error('Failed to write crash report')
  })
  console.log('Crash report received:', sanitizeForLog(entry.id || 'unknown'))
  res.json({ status: 'ok' })
})

app.listen(PORT, () => {
  console.log(`QuickDash API running on http://localhost:${PORT}`)
})
