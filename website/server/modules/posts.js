import { Router } from 'express'
import { readFile } from 'node:fs/promises'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const DATA_PATH = join(__dirname, '..', 'data', 'posts.json')

const router = Router()

router.get('/', async (req, res) => {
  try {
    const raw = await readFile(DATA_PATH, 'utf-8')
    let posts = JSON.parse(raw)
    const { limit, category } = req.query
    if (category) posts = posts.filter(p => p.category === category)
    if (limit) posts = posts.slice(0, parseInt(limit, 10))
    res.json(posts)
  } catch (err) {
    console.error('Error reading posts:', err.message)
    res.status(500).json({ error: 'Failed to read posts' })
  }
})

export default router
