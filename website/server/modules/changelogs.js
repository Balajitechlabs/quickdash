import { Router } from 'express'
import { readFile } from 'node:fs/promises'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const DATA_PATH = join(__dirname, '..', 'data', 'changelogs.json')

const router = Router()

router.get('/', async (req, res) => {
  try {
    const raw = await readFile(DATA_PATH, 'utf-8')
    let releases = JSON.parse(raw)
    const { limit, version } = req.query
    if (version) releases = releases.filter(r => r.version === version)
    if (limit) releases = releases.slice(0, parseInt(limit, 10))
    res.json(releases)
  } catch (err) {
    console.error('Error reading changelogs:', err.message)
    res.status(500).json({ error: 'Failed to read changelogs' })
  }
})

export default router
