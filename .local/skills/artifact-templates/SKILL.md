---
name: artifact-templates
description: Apply a user's saved artifact template (a reusable slides/web style donor) when they ask to build or restyle an artifact "with my template", "using my saved template", "in my brand style", or refer to a template by name. Use this to discover the user's saved templates and materialize one for use.
---

# Artifact Templates

A user can save an existing slides or web artifact "as a template" — a reusable visual-language donor (theme/CSS tokens, fonts, layout components, assets). When the user asks to build a new artifact with one of their saved templates, or to restyle an existing artifact to match one, use the `useArtifactTemplate` callback to find and materialize it.

## When to Use

- The user refers to "my template", "my saved template", "my brand style", or a template by name.
- The user wants a new slides/web artifact to match the look of a previously saved one.
- The user wants to restyle an existing artifact to match a saved template.

## When NOT to Use

- The user has not mentioned a saved template and just wants a fresh design — author normally.
- The user wants to *save* an artifact as a template — that happens in the workspace UI, not here.

## Discover and apply

Call `useArtifactTemplate()` as a CodeExecution callback (await it inside a code block).

**List the user's saved templates** (no argument):

```javascript
const result = await useArtifactTemplate();
// { success: true, mode: "list", templates: [{ slug, name, description, artifactKind }, ...], instructions }
```

**Resolve and materialize a specific template** — pass a `query` (a template name, slug, or descriptive phrase):

```javascript
const result = await useArtifactTemplate({ query: "Acme pitch deck" });
// On a single match:
// { success: true, templateSlug, name, referencePath: ".local/artifact_templates/<slug>", instructions, ... }
```

Outcomes to handle:

- `success: true` with a `referencePath` — the template is materialized on disk. Read `<referencePath>/SKILL.md` and the raw files under `<referencePath>/artifact/`, then author/restyle in that visual language (see below).
- `errorCode: "TEMPLATE_NOT_FOUND"` — no match; the response lists `availableTemplates`. Confirm with the user.
- `errorCode: "TEMPLATE_AMBIGUOUS"` — multiple matches in `candidates`; ask the user which one, then call again with the exact `slug`.
- `errorCode: "TEMPLATE_SERVICE_UNAVAILABLE"` — transient; retry or proceed without the template.

## Using a materialized template

The template is a **style donor**, not content to copy:

- **New artifact:** author a fresh artifact about the user's topic in the template's visual language (theme, fonts, layout).
- **Restyle:** rewrite an existing artifact's styling in that visual language while preserving its existing content.

Do not copy the donor template's own content verbatim unless the user explicitly asks.
