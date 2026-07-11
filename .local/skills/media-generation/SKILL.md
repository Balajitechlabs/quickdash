---
name: media-generation
description: "Generate AI images. Use this skill for visual content creation. For AI video clips, read `media-generation/video-generation.md`; for 3D model assets, read `media-generation/reference/3d-model-generation.md`; for music, sound effects, and text-to-speech audio, read `media-generation/audio-generation.md`"
---

# Media Generation Skill

Generate custom images, videos, 3D models, music, sound effects, and text-to-speech audio. The TypeScript runtime currently registers `generateImage`, `generateVideo`, `generate3DModel`, `generateMusic`, `generateSoundEffect`, `searchVoices`, and `textToSpeech`.

## Available Functions

### generateImage({prompt, ...})

Generate one custom image from a text description. Await the returned promise before reading the generated file. If you need multiple images, start multiple `generateImage` calls and await them together.

**Parameters:**

- `prompt` (required): Text description of the desired image
- `outputPath` (required): Workspace-relative file path. Must end in `.png`, `.jpg`, or `.jpeg`; other extensions will cause an error. Use an unused path.
- `summary`: Optional, short 4-5 word description for the return description
- `removeBackground`: Optional boolean. Set to `true` when the result should be a transparent PNG with the background removed, such as logos, icons, stickers, product cutouts, or subject images that will be composited over another background.
- `resolution`: Optional `"low"` or `"high"`. Defaults to `"low"`. Use `"high"` when higher quality is worth the extra latency.

Both `"low"` and `"high"` support `.png`, `.jpg`, and `.jpeg` output paths. WebP is not accepted by this callback. When `removeBackground` is `true`, `outputPath` must end in `.png`.

**Returns:** A job that resolves to a dict with `filePath` and `description`

**Examples:**

```javascript
// Start early, await before consuming the file
const heroImage = generateImage({
  prompt: 'A serene mountain landscape at sunset with snow-capped peaks',
  outputPath: 'src/assets/images/hero.png',
});

// Do unrelated file/code work here.

const result = await heroImage;
console.log(`Image saved to: ${result.filePath}`);

// Single image
const result = await generateImage({
  prompt: 'A serene mountain landscape at sunset with snow-capped peaks',
  outputPath: 'src/assets/images/hero.png',
});
console.log(`Image saved to: ${result.filePath}`);

// Logo or icon with a transparent background
const logo = await generateImage({
  prompt: 'A simple friendly robot mascot icon, no text, no words, no letters',
  outputPath: 'src/assets/images/robot-logo.png',
  summary: 'robot logo',
  removeBackground: true,
});
console.log(`Transparent image saved to: ${logo.filePath}`);

// Higher-quality generation
const productShot = await generateImage({
  prompt: 'A polished studio product shot of a ceramic smart speaker',
  outputPath: 'src/assets/images/product-shot.jpg',
  summary: 'product shot',
  resolution: 'high',
});
console.log(`High-quality image saved to: ${productShot.filePath}`);

// Multiple images in parallel
const imageJobs = [
  generateImage({ prompt: 'A red apple', outputPath: 'assets/apple.png' }),
  generateImage({
    prompt: 'A yellow banana',
    outputPath: 'assets/banana.png',
  }),
  generateImage({ prompt: 'An orange', outputPath: 'assets/orange.png' }),
];
const images = await Promise.all(imageJobs);
for (const img of images) {
  console.log(`Generated: ${img.filePath}`);
}
```

## When to Use Each Function

### generateImage

- Custom illustrations or graphics not available elsewhere
- Specific visual concepts or designs
- Placeholder images for development
- Creative or artistic content

## Aspect Ratio Guidelines

### Images

`generateImage` does not accept an `aspectRatio` argument in this runtime. Describe the desired composition in the prompt, such as "wide 16:9 hero image" or "square icon on transparent-style background."

## Best Practices

1. **Write detailed prompts**: Include style, mood, lighting, colors, and composition
2. **Choose supported formats**: Use `.png`, `.jpg`, or `.jpeg`. Use `.png` when `removeBackground` is `true`.
3. **Start slow generations early**: Store each `generateImage` promise, do unrelated work, then `await` before using the file path.
4. **Describe composition in the prompt**: Include phrases like "wide 16:9 hero image" or "square app icon" when the image shape matters.
5. **Do not over generate**: Only generate multiple images when the user explicitly asks.

## Output Locations

- Generated images: `attached_assets/generated_images/`

## Limitations

- Complex or highly specific prompts may not match exactly
- Text in generated media is not reliably rendered

## Copyright

- Use this skill to create media assets instead of copying from websites
- Generated images are created for your use
