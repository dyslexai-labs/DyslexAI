# DyslexAI — Architecture

DyslexAI is an Android assistive-reading app for children with dyslexia. The hackathon demo is centered on **local Android execution** with **Gemma 4 E4B LiteRT-LM**, using one multimodal model to support image understanding, text simplification, guided reading, and speech-oriented practice.

## System overview

```text
Android app
  -> Vue + Capacitor interface
  -> native Capacitor bridge
  -> DyslexAIEngine
  -> GemmaLocalRuntime
  -> LiteRT-LM local engine
  -> local Gemma 4 E4B model on device
```

```text
                         ┌──────────────────────────┐
                         │      Android App          │
                         │   Vue + Capacitor UI      │
                         └─────────────┬────────────┘
                                       │
                                       ▼
                         ┌──────────────────────────┐
                         │  Native Android Bridge   │
                         │  Model check / download  │
                         │  Runtime initialization  │
                         └─────────────┬────────────┘
                                       │
                                       ▼
                         ┌──────────────────────────┐
                         │ Gemma 4 E4B LiteRT-LM    │
                         │ Local multimodal runtime │
                         └─────────────┬────────────┘
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        │                              │                              │
        ▼                              ▼                              ▼
┌──────────────────┐          ┌──────────────────┐          ┌──────────────────┐
│ Image workflow   │          │ Reading workflow │          │ Speech workflow  │
│ Text extraction  │          │ Simplification   │          │ Transcription    │
│ Page reading     │          │ Syllable support │          │ Reading feedback │
└────────┬─────────┘          └────────┬─────────┘          └────────┬─────────┘
         │                             │                             │
         └─────────────────────────────┼─────────────────────────────┘
                                       ▼
                         ┌──────────────────────────┐
                         │ Guided Reading Interface │
                         │ Line / word / syllables  │
                         │ PT / EN interaction      │
                         └──────────────────────────┘
```

## Architectural idea

DyslexAI treats Gemma as the central intelligence layer of the product rather than as a generic chatbot. The app gives the model narrow educational tasks through structured prompts, then presents the results inside a child-friendly reading interface.

That lets the same local model support several connected jobs:

- read visible text from a photographed page;
- simplify dense text while preserving the educational meaning;
- generate short reading exercises;
- transcribe a child's reading attempt;
- return feedback and syllabified text for guided practice.

## Main components

### Frontend

The frontend is built with **Vue 3** and packaged for Android with **Capacitor**. It handles:

- screen flow and navigation;
- image capture or gallery selection;
- guided reading views;
- original, simplified, and spoken-text modes;
- line-by-line, word-by-word, and syllable-assisted reading;
- Portuguese / English interface switching;
- model-download progress and first-launch setup.

### Native Android bridge

The Android layer connects the Capacitor UI to the local runtime. It is responsible for:

- checking whether the model already exists;
- downloading the model on first launch when needed;
- initializing the local engine;
- forwarding image, text, and audio requests between the UI and native code;
- reporting runtime state back to the app.

### DyslexAIEngine

`DyslexAIEngine` is the product-facing orchestration layer. It normalizes inputs and outputs for the frontend and defines the prompt contracts used for:

- image extraction and simplification;
- direct text simplification;
- reading-phrase generation;
- audio transcription;
- reading feedback;
- syllabified expected and spoken text.

### Local Gemma runtime

`GemmaLocalRuntime` loads **Gemma 4 E4B LiteRT-LM** through LiteRT-LM and runs local text, image, and audio inference on the Android device.

After the first model download, normal reading material can be processed on-device instead of being sent to a remote OCR or cloud inference service.

## Prompt design

Prompt engineering is part of the architecture, not a cosmetic layer. The app constrains Gemma toward educational outputs such as:

- extracting only the useful visible text from a photographed page;
- rewriting content in clearer language;
- keeping responses structured for the reader UI;
- generating feedback that is usable for a child rather than merely descriptive;
- producing syllable-separated forms that can be displayed in the interface.

The syllable flow is already implemented, but it remains an area where output quality can still improve through prompt refinement and validation.

## Why local inference?

Local inference helps the project pursue:

- greater privacy for children's reading material;
- lower dependence on remote services after setup;
- an offline-first usage model once the model is installed;
- a coherent demonstration of edge AI with Gemma;
- tighter control over latency, failure modes, and product experience.

## Development artifacts

The `kaggle/` folder preserves notebook experiments and bridge prototypes used while exploring Gemma workflows during development. The current hackathon demo, however, is centered on the **Android local-inference path**.

## Current scope

The current build includes:

- local image understanding;
- local text simplification;
- guided reading by line and word;
- syllable-assisted presentation;
- local speech-oriented reading practice;
- Portuguese and English UI support;
- first-launch model download and local initialization.

The next engineering frontier is not adding these flows from zero, but making them more robust: faster inference, stronger syllable quality, cleaner outputs, and broader device testing.
