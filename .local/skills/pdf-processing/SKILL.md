---
name: pdf-processing
description: Use when a PDF file is attached or uploaded. The default text extraction does not capture visual information — this skill lets you render PDF pages as images, extract embedded images, and visually understand charts, figures, diagrams, slides, headshots, and layout. Read this skill whenever you need to see what a PDF looks like rather than just reading its text.
---

# PDF Visual Processing

Process PDF documents visually — render pages to images, extract embedded images, and crop specific regions. This lets you **see** PDF content (slides, figures, charts, headshots, diagrams) rather than only reading extracted text.

## When to Use

- User asks about visual content in a PDF (images, figures, charts, layout, slide design)
- User wants to extract specific images from a PDF (headshots, logos, diagrams, photos)
- The text-only output from `read_file` doesn't capture the needed information
- User wants to convert PDF pages to images or recreate a slide design

## When NOT to Use

- The user only needs text content — use `read_file` directly
- The user wants to create a new PDF — use appropriate libraries for that

## Setup

Install PyMuPDF (self-contained, no system dependencies):

```bash
pip install pymupdf
```

Adapt for the repl's package manager if needed (`uv add pymupdf`, etc.).

## What You Must Do

After reading this skill, you must immediately write and run Python scripts using PyMuPDF (`import fitz`) to visually process the PDF. Do not skip this — the extracted text shown earlier cannot capture images, charts, layout, or design.

1. `pip install pymupdf` and `mkdir -p .agents/scripts .agents/outputs`
2. Write a script to `.agents/scripts/` that renders PDF pages to PNG images, then run it
3. Read the output images to see the actual visual content
4. Extract embedded images or crop specific regions as needed

## PyMuPDF API Reference

- **Render pages**: `page.get_pixmap(matrix=fitz.Matrix(zoom, zoom))` — zoom=2 for viewing (144 DPI), zoom=3 for assets (216 DPI)
- **Extract embedded images**: `page.get_images(full=True)` then `doc.extract_image(xref)` — original resolution
- **Crop a region**: `page.get_pixmap(matrix=mat, clip=fitz.Rect(x0, y0, x1, y1))`
- **Metadata**: `doc.page_count`, `doc.metadata`, `page.rect` for dimensions
- **Text with positions**: `page.get_text("dict")["blocks"]` — each block has `bbox` and `type` (0=text, 1=image)

## Key Facts

- PDF coordinates: origin is top-left, units are points (72/inch). Letter = 612x792, 16:9 slide = 720x405 or 960x540.
- Embedded image extraction gives original resolution; page rendering + cropping is limited to your zoom level.
- Create output directories before running scripts (`mkdir -p`).
