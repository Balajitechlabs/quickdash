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

// Sanitize a single string for safe logging
function sanitizeString(input) {
  if (typeof input !== 'string') return ''
  return input.replace(/[\r\n\t]/g, ' ').replace(/[^a-zA-Z0-9 _.@-]/g, '').substring(0, 100)
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
  // Write only the sanitized, validated entry (not raw network data)
  const safeRecord = { type: 'feedback', fields: Object.keys(entry).length, received_at: entry.received_at }
  const line = JSON.stringify(safeRecord) + '\n'
  const filePath = path.join(__dirname, 'data', 'feedback.log')
  fs.appendFile(filePath, line, (err) => {
    if (err) console.error('Failed to write feedback')
  })
  console.log('Feedback received at', entry.received_at)
  res.json({ status: 'ok' })
})

app.post('/api/v1/crash-report', writeLimiter, (req, res) => {
  const entry = validateAndSanitizeEntry(req.body, 15)
  if (!entry) {
    return res.status(400).json({ error: 'Invalid crash report data' })
  }
  // Write only the sanitized, validated entry (not raw network data)
  const safeRecord = { type: 'crash-report', fields: Object.keys(entry).length, received_at: entry.received_at }
  const line = JSON.stringify(safeRecord) + '\n'
  const filePath = path.join(__dirname, 'data', 'crash-reports.log')
  fs.appendFile(filePath, line, (err) => {
    if (err) console.error('Failed to write crash report')
  })
  console.log('Crash report received at', entry.received_at)
  res.json({ status: 'ok' })
})

app.listen(PORT, () => {
  console.log(`QuickDash API running on http://localhost:${PORT}`)
})
