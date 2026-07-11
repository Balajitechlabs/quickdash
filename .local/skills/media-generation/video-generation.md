# Video Generation

## Available Functions

### generateVideo({prompt, ...})

Generate short video clips from text descriptions.

**Parameters:**

- `prompt` (str, required): Detailed text description of the desired video
- `outputPath` (str, required): Destination path for the generated video. Use an unused workspace-relative `.mp4`, `.mov`, or `.webm` path.
- `aspectRatio` (str, default "16:9"): "16:9" (landscape) or "9:16" (portrait)
- `resolution` (str, default "720p"): "720p" or "1080p"
- `durationSeconds` (int, default 6): 4, 6, or 8 seconds
- `negativePrompt` (str, optional): Description of what should NOT appear
- `personGeneration` (str, optional): "dont_allow" or "allow_adult" for controlling people
- `highQuality` (bool, optional): Use the higher-quality, slower model

**Returns:** A job that resolves to a dict with `filePath` and `description` keys

**Example:**

```javascript
const videoJob = generateVideo({
    prompt: "A cat playing with a ball of yarn, cute and playful, natural lighting",
    outputPath: "attached_assets/generated_videos/playful-cat.mp4",
    aspectRatio: "16:9",
    durationSeconds: 6
});

// Do unrelated file/code work here.

const result = await videoJob;
console.log(`Video saved to: ${result.filePath}`);
```

## When to Use Each Function

### generateVideo

- Short animated clips or motion graphics
- Video backgrounds or visual effects
- Product animations or demonstrations
- Social media video content

## Aspect Ratio Guidelines

### Videos

- **16:9** - Widescreen landscape, good for web videos, presentations
- **9:16** - Vertical portrait, good for mobile stories, social media shorts

## Output Locations

- Generated videos: `attached_assets/generated_videos/`

## Limitations

- Generated videos: 8 seconds maximum

## Copyright

- Generated videos are created for your use
