# DyslexAI

**DyslexAI** is an assistive reading application for children with dyslexia, created for the **Kaggle Gemma 4 Good Hackathon**.

Our goal is to make reading more accessible, less frustrating, and more autonomous for children who struggle with dyslexia. DyslexAI combines **multimodal AI**, **text simplification**, and **guided reading support** to transform textbook pages into clearer, more readable learning experiences.

---

## Why this matters

Many children with dyslexia face barriers that go beyond decoding words. They often struggle with:

- dense or visually overwhelming text
- long sentences and complex structures
- reduced reading fluency
- low confidence when reading independently
- difficulty following the reading flow line by line or word by word

DyslexAI was designed to address these challenges with an experience that feels like a real educational product, not just a technical prototype.

---

## What DyslexAI does

DyslexAI is built to support reading in a practical school context.

A student can:

1. **Load an image** of a textbook page or worksheet
2. **Extract visible text** from the page using a Gemma-family multimodal model
3. **Simplify the text** into clearer European Portuguese
4. **Read with guidance**, using:
   - original text
   - simplified text
   - line-by-line reading
   - word-by-word reading
5. *(In development)* **Read aloud and be accompanied by the app**

The current mobile flow returns structured data ready for the reader interface:

- `original_text`
- `simplified_text`
- `original_lines`
- `simplified_lines`

---

## Hackathon focus

This project was developed in the context of the **Gemma 4 Good Hackathon**, with a clear social impact goal:

- **inclusive education**
- **reading accessibility**
- **AI applied to a real problem**
- **child-centered experience**
- **practical mobile demo**

Rather than building a generic chatbot or OCR utility, DyslexAI focuses on a specific educational challenge: helping children with dyslexia read more effectively and more independently.

---

## Current architecture

DyslexAI currently includes two complementary paths in the repository:

### 1. Original web / backend path

The repository still contains the original project structure:

- `frontend/` — Vue 3 + Vite interface
- `backend/` — Flask API and processing providers
- `kaggle/` — Kaggle notebook and bridge experiments with Gemma 4

### 2. Mobile local inference path

The Android app is built with **Capacitor** and includes a local inference runtime using **LiteRT-LM**.

Current mobile architecture:

```text
Android app
→ Vue interface
→ Capacitor plugin
→ DyslexAIEngine
→ GemmaLocalRuntime
→ LiteRT-LM local engine
→ local model on device
```

This mobile path is now a core part of the implementation.

---

## Current mobile capabilities

The current Android mobile runtime supports:

- local model initialization
- local text simplification
- local image-based text extraction
- structured output for guided reading

Current capability flags:

- `text: true`
- `image: true`
- `audio: false`
- `syllables: false`

Audio support is planned, but not yet implemented in the current mobile runtime.

---

## Current prompting approach

The mobile runtime currently uses prompts for:

- extracting only the visible text from an image
- simplifying text into clearer European Portuguese
- returning only the requested output
- keeping the experience focused and predictable

Prompting is still being refined to improve:

- latency
- output cleanliness
- readability for dyslexic students
- consistency in European Portuguese

---

## Android local runtime

The mobile runtime expects the local model file to be present on the Android device at:

```text
/data/local/tmp/llm/gemma/model.litertlm
```

The app checks this file during runtime initialization before creating the LiteRT-LM engine.

Typical development flow:

```bash
npm run build
npx cap sync android
npx cap open android
```

Then the app is run on a real Android device.

---

## Current status

### Working now

- Android local runtime initialization
- local text simplification
- local image extraction path
- guided reading data generation
- original/simplified reading modes
- line and word reading support

### In progress

- prompt tuning
- latency reduction on lower-end Android tablets
- better multimodal model support on-device
- stronger demo robustness

### Planned next

- child voice capture
- transcription of the child’s reading
- detection of common reading difficulties such as:
  - letter swaps
  - syllable swaps
  - missing or incorrect accentuation
  - other reading patterns relevant to dyslexia support

---

## Why DyslexAI is different

DyslexAI is **not** just:

- an OCR app
- a generic chatbot
- a simple text-to-speech interface

It is designed as a **guided reading experience** for children with dyslexia.

The aim is not only to extract text, but to make reading:

- clearer
- more manageable
- more adaptive
- more autonomous

---

## Roadmap

Short term:

- improve mobile latency
- refine prompts for better extraction and simplification
- improve visual polish for demo quality
- strengthen fallback behavior for reliable live demonstrations

Mid term:

- integrate child voice reading support
- compare spoken reading against displayed text
- identify common dyslexia-related reading patterns
- evolve into a stronger educational support product

---

## Repository structure

```text
DyslexAI/
├── backend/
├── frontend/
│   ├── src/
│   └── android/
├── kaggle/
└── README.md
```

---

## Vision

DyslexAI is being built not only as a hackathon project, but as the foundation for a real educational tool.

The long-term vision is to support children with dyslexia through:

- accessible reading workflows
- AI-assisted simplification
- guided reading support
- reading fluency development
- inclusive educational technology

---

## About

**DyslexAI**  
Assistive reading app for dyslexic students using AI.
