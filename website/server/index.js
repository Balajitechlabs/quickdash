import express from 'express'
import cors from 'cors'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import postsRouter from './modules/posts.js'
import changelogsRouter from './modules/changelogs.js'
import docsRouter from './modules/docs.js'
import updatesRouter from './modules/updates.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const app = express()
const PORT = process.env.PORT || 4000

app.use(cors())
app.use(express.json())

app.use('/api/reading/posts', postsRouter)
app.use('/api/reading/changelogs', changelogsRouter)
app.use('/api/reading/docs', docsRouter)
app.use('/api/reading/updates', updatesRouter)

app.get('/api/reading/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() })
})

app.post('/api/v1/feedback', (req, res) => {
  const entry = { ...req.body, received_at: new Date().toISOString() }
  const line = JSON.stringify(entry) + '\n'
  const filePath = path.join(__dirname, 'data', 'feedback.log')
  fs.appendFile(filePath, line, (err) => {
    if (err) console.error('Failed to write feedback:', err)
  })
  console.log('Feedback received:', entry)
  res.json({ status: 'ok' })
})

app.post('/api/v1/crash-report', (req, res) => {
  const entry = { ...req.body, received_at: new Date().toISOString() }
  const line = JSON.stringify(entry) + '\n'
  const filePath = path.join(__dirname, 'data', 'crash-reports.log')
  fs.appendFile(filePath, line, (err) => {
    if (err) console.error('Failed to write crash report:', err)
  })
  console.log('Crash report received:', entry.id)
  res.json({ status: 'ok' })
})

app.listen(PORT, () => {
  console.log(`QuickDash API running on http://localhost:${PORT}`)
})
