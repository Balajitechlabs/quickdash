import fs from 'fs';
import { parseArgs as parseNodeArgs } from 'node:util';
import path from 'path';

const TEMPLATE_SUFFIX = '.template';
const HTML_FILE_EXTENSIONS = new Set(['.html', '.htm']);
const TOKEN_VALUES = {
  __REPLIT_ARTIFACT_SLUG__: (slug) => slug,
  __REPLIT_ARTIFACT_TITLE__: (_, title) => title,
  __REPLIT_ARTIFACT_PACKAGE_NAME__: (slug) => slug,
};

function writeStdout(message) {
  process.stdout.write(`${message}\n`);
}

function writeStderr(message) {
  process.stderr.write(`${message}\n`);
}

function escapeHtml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function parseArgs(argv) {
  const { values, positionals } = parseNodeArgs({
    args: argv.slice(2),
    allowPositionals: true,
    options: {
      slug: { type: 'string' },
      title: { type: 'string' },
    },
  });
  const [artifactType] = positionals;
  const slug = values.slug;
  const title = values.title;

  if (!artifactType || !slug || !title) {
    writeStderr(
      'Usage: node bootstrap-legacy.js <artifactType> --slug=<slug> --title=<title>',
    );
    process.exit(1);
  }

  return { artifactType, slug, title };
}

function interpolate(content, slug, title, { isHtml }) {
  let rendered = content;
  for (const [token, resolver] of Object.entries(TOKEN_VALUES)) {
    const value = resolver(slug, title);
    const replacement = isHtml ? escapeHtml(value) : value;
    rendered = rendered.replaceAll(token, replacement);
  }
  return rendered;
}

function copyDir(src, dest, slug, title) {
  fs.mkdirSync(dest, { recursive: true });

  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, entry.name);
    let destName = entry.name;

    if (entry.isDirectory()) {
      copyDir(srcPath, path.join(dest, destName), slug, title);
      continue;
    }

    const isTemplate = destName.endsWith(TEMPLATE_SUFFIX);
    if (isTemplate) {
      destName = destName.slice(0, -TEMPLATE_SUFFIX.length);
    }

    const destPath = path.join(dest, destName);

    if (isTemplate) {
      const raw = fs.readFileSync(srcPath, 'utf8');
      const isHtml = HTML_FILE_EXTENSIONS.has(
        path.extname(destName).toLowerCase(),
      );
      fs.writeFileSync(destPath, interpolate(raw, slug, title, { isHtml }));
      continue;
    }

    fs.copyFileSync(srcPath, destPath);
  }
}

function main() {
  const { artifactType, slug, title } = parseArgs(process.argv);
  const workspaceRoot = process.cwd();

  let artifactFilesDir = artifactType;
  if (artifactType === 'data-visualization') {
    artifactFilesDir = 'react-vite';
  }

  const filesDir = path.join(
    import.meta.dirname,
    'artifacts',
    artifactFilesDir,
    'files',
  );
  const destDir = path.join(workspaceRoot, 'artifacts', slug);

  if (!fs.existsSync(filesDir)) {
    writeStderr(`Error: missing template directory for ${artifactFilesDir}`);
    process.exit(1);
  }

  if (fs.existsSync(destDir)) {
    writeStderr(`Error: artifacts/${slug}/ already exists`);
    process.exit(1);
  }

  writeStdout(`Bootstrapping ${artifactType} artifact: ${slug}`);

  copyDir(filesDir, destDir, slug, title);
  writeStdout(`  Copied files to artifacts/${slug}/`);

  writeStdout('Done.');
}

main();
