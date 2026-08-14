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

app.post('/api/v1/feedback', writeLimiter, (req, res) => {
  if (!req.body || typeof req.body !== 'object' || Array.isArray(req.body)) {
    return res.status(400).json({ error: 'Invalid feedback data' })
  }
  const timestamp = new Date().toISOString()
  const logData = JSON.stringify({ event: 'feedback', timestamp }) + '\n'
  const filePath = path.join(__dirname, 'data', 'feedback.log')
  fs.appendFile(filePath, logData, (err) => {
    if (err) console.error('Failed to append feedback log')
  })
  console.log('Feedback submission received')
  res.json({ status: 'ok' })
})

app.post('/api/v1/crash-report', writeLimiter, (req, res) => {
  if (!req.body || typeof req.body !== 'object' || Array.isArray(req.body)) {
    return res.status(400).json({ error: 'Invalid crash report data' })
  }
  const timestamp = new Date().toISOString()
  const logData = JSON.stringify({ event: 'crash-report', timestamp }) + '\n'
  const filePath = path.join(__dirname, 'data', 'crash-reports.log')
  fs.appendFile(filePath, logData, (err) => {
    if (err) console.error('Failed to append crash report log')
  })
  console.log('Crash report submission received')
  res.json({ status: 'ok' })
})

app.listen(PORT, () => {
  console.log(`QuickDash API running on http://localhost:${PORT}`)
})
