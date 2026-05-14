# DyslexAI — Android Installation Guide

This guide explains how to install and run the DyslexAI Android demo.

## 1. Install the APK

Download the APK from the GitHub Release:

**DyslexAI-Hackathon-Demo-v1.0.apk**

Install it on an Android device.

If Android asks for permission to install apps from unknown sources, allow it for this installation.

## 2. First launch

Open DyslexAI.

On the first launch, the app checks if the local Gemma model is already available.

If the model is missing, the app automatically downloads it.

## 3. Model download

The app downloads the following model:

**Gemma 4 E4B LiteRT-LM**

Source:

https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm

The download screen shows:

- download percentage
- progress bar
- downloaded MB / total MB
- Wi-Fi recommendation

The model is not included in the APK or in the GitHub repository.

## 4. Local execution

After the model is installed, DyslexAI runs locally on the Android device.

The reading assistance flow uses local inference for:

- image understanding
- text extraction
- text simplification
- guided reading support

## 5. Recommended device

Recommended:

- Android device
- stable Wi-Fi for first model download
- enough free storage for the model
- modern mid-range or high-end device for better performance

## 6. Troubleshooting

### The model download fails

Check:

- internet connection
- available storage
- Wi-Fi connection

Then open the app and retry the download.

### The app is slow on first use

The first launch may take longer because the model needs to be downloaded and initialized.

### The app does not start inference

Close and reopen the app after the model download finishes.