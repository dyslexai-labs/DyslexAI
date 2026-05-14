# DyslexAI — Architecture

DyslexAI is an Android reading-assistance app for children with dyslexia.

The final hackathon version focuses on local Android execution using **Gemma 4 E4B LiteRT-LM**, taking advantage of Gemma’s multimodal and multilingual capabilities for accessible reading support.

## Overview

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
│ Vision support   │          │ Text generation  │          │ Speech support   │
│ Image reading    │          │ Simplification   │          │ Reading feedback │
│ Text extraction  │          │ Explanations     │          │ Audio assistance │
└────────┬─────────┘          └────────┬─────────┘          └────────┬─────────┘
         │                             │                             │
         └─────────────────────────────┼─────────────────────────────┘
                                       ▼
                         ┌──────────────────────────┐
                         │ Guided Reading Interface │
                         │ Line / word support      │
                         │ PT / EN interface        │
                         └──────────────────────────┘
```

## Core idea

DyslexAI uses Gemma as the central intelligence layer of the application.

Instead of combining multiple external tools for OCR, summarisation, translation or speech-related support, the project explores how a single Gemma-based local runtime can support several accessibility tasks through well-designed prompts and a child-friendly interface.

## Why Gemma?

Gemma is especially relevant to DyslexAI because it can support, out of the box, several capabilities that are important for accessible reading:

- **Vision understanding** — analysing images of school materials and extracting relevant text.
- **Text generation** — rewriting dense or complex text into simpler language.
- **Multilingual interaction** — supporting Portuguese and English user interfaces and future multilingual reading experiences.
- **Speech-related support** — enabling future reading feedback and oral reading assistance workflows.
- **Prompt-driven task optimisation** — adapting the model behaviour to specific educational tasks through carefully designed instructions.

This means the app is not built as a generic OCR tool. It is designed as a reading-assistance experience where Gemma is guided to perform educational and accessibility-oriented tasks.

## Prompt design as part of the architecture

Prompt engineering is an important architectural component of DyslexAI.

The app uses structured prompts to guide Gemma towards specific behaviours, such as:

- extracting only the useful text from a photographed page;
- simplifying text without changing its educational meaning;
- producing output suitable for children with dyslexia;
- keeping responses clear, short and readable;
- supporting guided reading modes;
- preparing feedback that encourages autonomous reading.

This approach improves both performance and task effectiveness because the model is not asked to behave like a general chatbot. It is given a precise educational role.

## Main components

### Frontend

The user interface is built with Vue and packaged for Android using Capacitor.

The frontend is responsible for:

- screen flow;
- image selection;
- guided reading interface;
- Portuguese / English language selection;
- audio and reading interaction;
- model download progress display;
- accessible visual presentation for children with reading difficulties.

### Android layer

The Android layer connects the Capacitor frontend with the local inference runtime.

It handles:

- local model detection;
- automatic model download;
- progress reporting during model download;
- model initialization;
- communication between Vue and native Android code.

### Local Gemma runtime

The app uses a local Gemma 4 E4B LiteRT-LM model.

The model is downloaded on first launch and then used directly on the Android device.

This avoids sending children’s reading material to external OCR or cloud processing services during normal use after setup.

### Kaggle experiments

The `kaggle/` folder contains supporting experiments and demonstrations used during development.

These experiments helped validate the use of Gemma for image understanding, text extraction, simplification and accessibility-oriented workflows.

The final demo app is focused on Android/local execution.

## Why local inference?

Local inference helps with:

- privacy;
- reduced dependency on cloud services;
- offline-first usage after model setup;
- better control over the user experience;
- suitability for educational accessibility scenarios;
- demonstrating practical edge AI with Gemma.

## Final hackathon focus

The final version prioritises:

- a working Android demo;
- real-device execution;
- Gemma 4 local inference;
- vision, text and speech-oriented accessibility workflows;
- multilingual readiness;
- dyslexia-focused reading support;
- clear installation;
- simple and accessible user experience.
