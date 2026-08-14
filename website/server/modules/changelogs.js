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
    if (version && typeof version === 'string') {
      const safeVersion = version.substring(0, 20)
      releases = releases.filter(r => r.version === safeVersion)
    }
    if (limit) {
      const num = Math.min(Math.max(parseInt(limit, 10) || 10, 1), 100)
      releases = releases.slice(0, num)
    }
    res.json(releases)
  } catch (err) {
    console.error('Error reading changelogs')
    res.status(500).json({ error: 'Failed to read changelogs' })
  }
})

export default router
