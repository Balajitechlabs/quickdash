import { Router } from 'express'
import { readFile } from 'node:fs/promises'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const POSTS_PATH = join(__dirname, '..', 'data', 'posts.json')
const CHANGELOGS_PATH = join(__dirname, '..', 'data', 'changelogs.json')

const router = Router()

router.get('/', async (req, res) => {
  try {
    const [postsRaw, releasesRaw] = await Promise.all([
      readFile(POSTS_PATH, 'utf-8').catch(() => '[]'),
      readFile(CHANGELOGS_PATH, 'utf-8').catch(() => '[]'),
    ])
    const posts = JSON.parse(postsRaw)
    const releases = JSON.parse(releasesRaw)
    const latestPost = posts[0]
    const latestRelease = releases[0]

    const updates = []
    if (latestPost) {
      updates.push({
        id: `post-${latestPost.id}`,
        type: 'content',
        title: latestPost.title,
        summary: latestPost.excerpt,
        version: '1.0',
        link: '/blog#' + latestPost.id,
      })
    }
    if (latestRelease) {
      updates.push({
        id: `release-${latestRelease.version}`,
        type: 'apk',
        title: latestRelease.title,
        summary: `Version ${latestRelease.version} is now available with ${latestRelease.highlights?.length || 0} changes.`,
        version: latestRelease.version,
        link: '/changelog',
      })
    }

    res.json(updates[0] || null)
  } catch (err) {
    console.error('Error fetching updates')
    res.json(null)
  }
})

export default router
