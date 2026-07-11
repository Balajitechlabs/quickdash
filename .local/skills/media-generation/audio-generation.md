# Audio Generation

`generateMusic`, `generateSoundEffect`, and `textToSpeech` run as background
jobs: start them early, do unrelated work, and await the promise before
reading the generated file. Unawaited calls surface a job id you can join
later with `await waitForJob({ jobId })`, including in a later CodeExecution
call.

## Available Functions

### generateMusic({prompt, ...})

Generate original music from a text prompt and save it as an audio file.

**Parameters:**

- `prompt` (str, required): Description of the desired music
- `outputPath` (str, optional): Must end in `.mp3` for MP3 output or `.ogg` for Opus output. Defaults to `attached_assets/generated_audio/music_{summary}.mp3`
- `summary` (str, default "generated_music"): Short description for the filename
- `durationSeconds` (int, optional): 3-600 seconds. If omitted, the model chooses a length
- `forceInstrumental` (bool, default True): Generate instrumental music
- `outputFormat` (str, default "mp3_44100_128"): Audio format
  - Options are alaw_8000, mp3_22050_32, mp3_24000_48, mp3_44100_128, mp3_44100_192, mp3_44100_32, mp3_44100_64, mp3_44100_96, opus_48000_128, opus_48000_192, opus_48000_32, opus_48000_64, opus_48000_96, pcm_16000, pcm_22050, pcm_24000, pcm_32000, pcm_44100, pcm_48000, pcm_8000, ulaw_8000, wav_16000, wav_22050, wav_24000, wav_32000, wav_44100, wav_48000, wav_8000
- `overwrite` (bool, default True): Whether to overwrite existing files

**Returns:** Dict with `filePath` and `description` keys

**Example:**

```javascript
// Start early, await before consuming the file
const music = generateMusic({
    prompt: "Upbeat electronic background music with warm synths and a steady pulse",
    summary: "upbeat synth bed",
    durationSeconds: 30,
    forceInstrumental: true
});
// Do unrelated file/code work here.
const result = await music;
console.log(`Music saved to: ${result.filePath}`);
```

The await does not have to happen in the same code block. If the block ends
while the generation is still pending, the output reports the pending job's
id, and a later code block can pick it up:

```javascript
// Later code block: join the job reported by the earlier block.
const result = await waitForJob({ jobId: "<jobId from the earlier block's output>" });
console.log(`Music saved to: ${result.filePath}`);
```

### generateSoundEffect({prompt, ...})

Generate a short sound effect from a text prompt and save it as an audio file.

**Parameters:**

- `prompt` (str, required): Description of the sound effect
- `outputPath` (str, optional): Must end in `.mp3` for MP3 output or `.ogg` for Opus output. Defaults to `attached_assets/generated_audio/sound_effect_{summary}.mp3`
- `summary` (str, default "sound_effect"): Short description for the filename
- `durationSeconds` (number, optional): 0.5-30 seconds. If omitted, the model chooses a length
- `loop` (bool, default False): Create a smoothly looping sound effect
- `promptInfluence` (number, default 0.3): 0-1, higher follows the prompt more closely
- `outputFormat` (str, default "mp3_44100_128"): Audio format
- `overwrite` (bool, default True): Whether to overwrite existing files

**Returns:** Dict with `filePath` and `description` keys

**Example:**

```javascript
const result = await generateSoundEffect({
    prompt: "Soft futuristic UI confirmation chime, clean and pleasant",
    summary: "ui confirmation chime",
    durationSeconds: 1.5
});
console.log(`Sound effect saved to: ${result.filePath}`);
```

### searchVoices({search, ...})

Search available voices for text-to-speech. Use this before `textToSpeech` when you do not know the `voiceId`.

**Parameters:**

- `search` (str, optional): Search by name, description, or labels
- `pageSize` (int, default 10): Number of voices to return, 1-100
- `voiceType` (str, optional): "personal", "community", "default", "workspace", "non-default", "non-community", or "saved"
- `category` (str, optional): "premade", "cloned", "generated", or "professional"
- `sort` (str, optional): "created_at_unix" or "name"
- `sortDirection` (str, optional): "asc" or "desc"
- `nextPageToken` (str, optional): Token for pagination

**Returns:** Dict containing `voices` (each with `voiceId`, `name`, `category`, `description`, and `labels`), `hasMore`, `totalCount`, and `nextPageToken`.

### textToSpeech({text, voiceId, ...})

Convert text into speech using a selected voice and save it as an audio file.

**Parameters:**

- `text` (str, required): Text to convert into speech
- `voiceId` (str, required): Voice ID from `searchVoices`
- `outputPath` (str, optional): Must end in `.mp3` for MP3 output or `.ogg` for Opus output. Defaults to `attached_assets/generated_audio/speech_{summary}.mp3`
- `summary` (str, default "speech"): Short description for the filename
- `modelId` (str, default "eleven_multilingual_v2"): Text-to-speech model ID
- `outputFormat` (str, default "mp3_44100_128"): Audio format
- `voiceSettings` (object, optional): Per-request voice settings such as `stability`, `similarity_boost`, `style`, `use_speaker_boost`, and `speed`
- `overwrite` (bool, default True): Whether to overwrite existing files

**Example:**

```javascript
const voices = await searchVoices({ search: "warm narrator", pageSize: 5 });
const voiceId = voices.voices[0].voiceId;
const result = await textToSpeech({
    text: "Welcome to a faster way to build.",
    voiceId,
    summary: "welcome voiceover"
});
console.log(`Speech saved to: ${result.filePath}`);
```

## When to Use Each Function

### generateMusic

- Original background music, loops, jingles, and music beds
- Instrumental tracks for videos, games, presentations, or apps

### generateSoundEffect

- UI sounds, notification tones, transitions, ambience, and game SFX
- Short audio moments for motion graphics or interactive experiences

### searchVoices / textToSpeech

- Voiceovers, narration, read-aloud content, and character speech
- Use `searchVoices` first when the voice ID is not known

## Best Practices

1. **Write detailed prompts**: Include genre, mood, instrumentation, pacing, duration, and intended use
2. **Start slow generations early**: Store each generation promise, do unrelated work, then `await` before using the file path. Independent clips can run in parallel with `Promise.all`.

## Output Locations

- Generated audio: `attached_assets/generated_audio/`

## Limitations

- Generated sound effects: 30 seconds maximum
- Generated music: 600 seconds maximum

## Copyright

- Generated music, sound effects, and speech are created for your use
