# DyslexAI

**DyslexAI** is an Android assistive-reading app for children with dyslexia, built for the **Kaggle Gemma 4 Good Hackathon**.

It turns a photographed school page into a calmer reading experience: Gemma extracts the text, rewrites it in clearer language, and the app guides the child through it line by line or word by word. The project is designed around one practical question: **how can multimodal AI reduce the friction between a child and the page in front of them?**

---

## What the app does

From a single mobile flow, a student can:

1. take a photo or choose an image of a worksheet, textbook page, or printed material;
2. extract the visible text with local multimodal inference;
3. receive a simplified version in clearer European Portuguese or English;
4. read with support through:
   - original and simplified text views;
   - line-by-line reading;
   - word-by-word reading;
   - adjustable text size, reading speed, and color palette;
   - audio playback;
5. practise reading aloud through a speech flow that can generate a phrase, record the child, transcribe the attempt, and return guided feedback.

### Current mobile capabilities

| Capability | Status |
| --- | --- |
| Text simplification | Working |
| Image understanding and text extraction | Working |
| Guided reading interface | Working |
| Local audio transcription / reading feedback | Working |
| Syllable segmentation | Implemented; needs further refinement |

The mobile runtime currently exposes:

```text
text: true
image: true
audio: true
syllables: true
```

---

## Why it matters

For many children with dyslexia, the obstacle is not only the words themselves. Dense layouts, long sentences, reduced reading fluency, and the effort of tracking text can turn ordinary school material into a wall.

DyslexAI is not intended to be just OCR, a chatbot, or text-to-speech stitched together. It is a **guided reading experience** built around accessibility, autonomy, and educational usefulness.

---

## Demo flow

| Home | Image selection | Guided reading |
| --- | --- | --- |
| ![Home screen](screenshots/EN/1-home-EN.png) | ![Choose image source](screenshots/EN/2-choose-image-source-EN.png) | ![Simplified guided reading](screenshots/EN/4-image-reading-simplified-EN.png) |

| Speech practice | Reading feedback |
| --- | --- |
| ![Reading from speech](screenshots/EN/5-reading-from-speech-EN.png) | ![Speech feedback results](screenshots/EN/6-results-feedback-reading-EN.png) |

For the full walkthrough, see [`docs/demo-flow.md`](docs/demo-flow.md).

---

## How Gemma is used

DyslexAI uses **Gemma 4 E4B LiteRT-LM** as the app's local multimodal engine. Prompted for narrow educational tasks, the same model supports:

- image understanding and visible-text extraction;
- text simplification for younger readers;
- short phrase generation for reading exercises;
- audio transcription and reading-feedback workflows;
- multilingual interaction in Portuguese and English.

The architecture deliberately keeps Gemma close to the user: after the first model download, inference runs on-device rather than sending a child's reading material to a remote service.

```text
Android app
  -> Vue + Capacitor interface
  -> native Capacitor bridge
  -> DyslexAIEngine
  -> GemmaLocalRuntime
  -> LiteRT-LM local engine
  -> local Gemma model on device
```

A fuller explanation is available in [`docs/architecture.md`](docs/architecture.md).

---

## Installation

### Fastest route: install the Android demo

1. Install `release/apk/DyslexAI-Hackathon-Demo-v1.0.apk` on a real Android device.
2. Open the app.
3. On first launch, the app downloads the Gemma model if it is not already present.
4. After setup, use the app locally on-device.

The model is **not bundled inside the APK**. On first launch, the app downloads:

```text
gemma-4-E4B-it.litertlm
```

from the LiteRT community model repository on Hugging Face.

For device notes and troubleshooting, see [`docs/install-guide.md`](docs/install-guide.md).

### Development build

```bash
cd frontend
npm install
npm run build
npx cap sync android
npx cap open android
```

Then run the project on a real Android device.

---

## Frontend configuration

The Vue frontend uses Vite variables. Create `frontend/.env` when needed:

```bash
cd frontend
touch .env
```

Useful variables:

```env
VITE_DYSLEXAI_MOCK=false
VITE_IMAGE_INFERENCE_TIMEOUT_MS=900000
```

`VITE_*` values are read at build time. If you change them for Android, rebuild and resync:

```bash
cd frontend
npm run build
npx cap sync android
```

Use mock mode only for interface development:

```env
VITE_DYSLEXAI_MOCK=true
```

---

## Repository map

```text
DyslexAI/
├── docs/          # architecture, demo flow, installation guide
├── frontend/      # Vue app, Capacitor Android project, local runtime bridge
├── release/apk/   # installable Android demo build
├── screenshots/   # PT and EN product screenshots
└── README.md
```


---

## Project status

### Working now

- Android model setup and runtime initialization;
- local text simplification;
- local image-based text extraction;
- local speech-oriented reading flow;
- guided reading with original/simplified modes;
- Portuguese and English interface support.

### Next improvements

- improved syllable segmentation quality;
- faster inference on weaker Android devices;
- stronger prompt consistency and output cleanup;
- richer dyslexia-oriented feedback patterns;
- broader testing across educational materials and devices.

---

## Documentation

- [`docs/architecture.md`](docs/architecture.md) — system design and Gemma role
- [`docs/demo-flow.md`](docs/demo-flow.md) — recommended hackathon walkthrough
- [`docs/install-guide.md`](docs/install-guide.md) — Android installation steps

---

## Vision

DyslexAI is a hackathon project, but it is aimed at a real educational horizon: reading support that is private, practical, and humane enough for children to use without feeling that technology has taken over the room.
