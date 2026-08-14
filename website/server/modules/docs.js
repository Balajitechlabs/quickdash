import { Router } from 'express'
import { readFile } from 'node:fs/promises'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const DATA_PATH = join(__dirname, '..', 'data', 'docs.json')

const router = Router()

router.get('/', async (req, res) => {
  try {
    const raw = await readFile(DATA_PATH, 'utf-8')
    let docs = JSON.parse(raw)
    const { id, section } = req.query
    if (id && typeof id === 'string') {
      const safeId = id.substring(0, 50)
      docs = docs.filter(d => d.id === safeId)
    }
    if (section && typeof section === 'string') {
      const safeSection = section.substring(0, 50)
      docs = docs.filter(d => d.section === safeSection)
    }
    res.json(docs)
  } catch (err) {
    console.error('Error reading docs')
    res.status(500).json({ error: 'Failed to read docs' })
  }
})

export default router
