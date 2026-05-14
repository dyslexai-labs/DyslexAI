# DyslexAI — Demo Flow

This document describes the recommended demo flow for the DyslexAI Android hackathon version.

The goal of the demo is to show DyslexAI as a working Android application that uses Gemma 4 locally to support children with dyslexia through image understanding, text simplification, guided reading and audio/speech interaction.

## 1. Open the App

Start DyslexAI on the Android device.

The home screen presents the app as a guided reading assistant for children with dyslexia. The interface is simple, visual and designed to reduce cognitive overload.

![Home Screen](../screenshots/EN/home.png)

## 2. Model Check and First Launch

On launch, the app checks if the local Gemma 4 LiteRT-LM model is available.

If the model is missing, DyslexAI displays the model preparation screen and downloads the model automatically.

The screen shows:

- download percentage
- progress bar
- downloaded MB / total MB
- Wi-Fi recommendation

![Model Download](../screenshots/EN/model-download.png)

This avoids requiring users or judges to manually copy the model to the Android device.

## 3. Select an Image

The user selects an image of a school text, book page, worksheet or printed content.

The image can come from:

- camera
- gallery

![Image Selection](../screenshots/EN/image-selection.png)

## 4. Process the Image Locally

DyslexAI sends the image to the local Gemma 4 LiteRT-LM runtime.

Gemma is used to:

- understand the image
- extract relevant text
- interpret the reading content
- prepare the content for accessibility-focused simplification

![Processing](../screenshots/EN/processing.png)

## 5. Text Extraction and Simplification

Gemma 4 is used not only to read the image, but also to transform the extracted content into a simpler and more accessible version.

The goal is not just transcription. The goal is to support reading comprehension for children with dyslexia.

![Simplification](../screenshots/EN/simplification.png)

## 6. Guided Reading

The simplified content is presented in a guided reading interface.

The child can follow the text step by step, reducing visual overload and improving focus during reading.

![Guided Reading](../screenshots/EN/guided-reading.png)

## 7. Audio / Speech Support

The app also supports audio or speech-related interaction, depending on the selected flow.

This supports the idea of helping the child practise reading more independently.

![Audio Reading](../screenshots/EN/audio-reading.png)

## 8. Language Support

DyslexAI includes Portuguese and English interface support.

This demonstrates that the project is prepared for multilingual accessibility scenarios.

![Language Support](../screenshots/EN/language-support.png)

## 9. Why Gemma 4 Matters in the Demo

The demo highlights how Gemma 4 can be used as a multimodal and multilingual local AI layer.

DyslexAI benefits from Gemma capabilities such as:

- vision understanding
- text generation
- text simplification
- multilingual support
- speech/audio-oriented interaction flows
- prompt-based task adaptation

With carefully designed prompts, the same local model can support different reading assistance tasks without depending on separate OCR, translation or simplification services.

## 10. Final Message

End the demo by highlighting:

- real Android execution
- local Gemma 4 inference
- automatic model preparation
- dyslexia-focused reading support
- privacy-friendly design
- multilingual accessibility
- inclusive education impact

DyslexAI is not just a text extraction tool. It is an assisted reading experience designed to help children access written content with more confidence and autonomy.
