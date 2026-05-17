# DyslexAI — Android Installation Guide

This guide explains how to install and run the DyslexAI Android demo.

## What you need

- a real Android device;
- stable Wi-Fi for the first model download;
- enough free storage for the local Gemma model;
- preferably a modern mid-range or high-end device for smoother inference.

## 1. Install the APK

Install:

```text
release/apk/DyslexAI-Hackathon-Demo-v1.0.apk
```

If Android asks for permission to install apps from unknown sources, allow it for this installation.

## 2. Open the app for the first time

On first launch, DyslexAI checks whether the local Gemma model is already available.

If the model is missing, the app opens a preparation screen and downloads it automatically.

## 3. Let the model download finish

The app downloads **Gemma 4 E4B LiteRT-LM** on first launch. The model is not bundled inside the APK and is not stored in the repository.

During setup, the app shows:

- download percentage;
- progress bar;
- downloaded size versus total size;
- a Wi-Fi recommendation.

Once the model is installed, the app initializes the local runtime and opens the normal DyslexAI interface.

## 4. Use the app locally

After setup, DyslexAI can run local inference for:

- image understanding;
- visible-text extraction;
- text simplification;
- guided reading;
- speech-oriented reading practice;
- syllable-assisted display.

## 5. Development workflow

For a local development build:

```bash
cd frontend
npm install
npm run build
npx cap sync android
npx cap open android
```

Then run the Android project on a real device.

## Troubleshooting

### The model download fails

Check:

- internet connection;
- available storage;
- Wi-Fi stability.

Then reopen the app and retry the download.

### The first launch feels slow

That is expected while the model is being downloaded and initialized for the first time.

### Inference does not start after setup

Close and reopen the app after the model download finishes.

### The app runs slowly

Inference speed depends on the Android device. A more capable device gives a smoother demo experience, especially for image and audio flows.
