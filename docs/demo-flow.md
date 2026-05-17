# DyslexAI — Demo Flow

This is the recommended walkthrough for presenting the DyslexAI Android hackathon demo.

## 1. Start at the home screen

The home screen introduces the two main paths:

- **assisted reading from an image**;
- **reading practice from speech**.

![Home Screen](../screenshots/EN/1-home-EN.png)

## 2. Choose a reading source

For the image path, the child can:

- take a photo;
- choose an image from the gallery.

This keeps the demo close to real school use: textbook pages, worksheets, and printed exercises.

![Choose Image Source](../screenshots/EN/2-choose-image-source-EN.png)

## 3. Confirm the image

Before inference starts, the app asks the user to confirm that the selected image is readable. This small step improves the quality of local multimodal processing and makes the flow feel deliberate rather than magical in a brittle way.

![Confirm Image](../screenshots/EN/3-confirm-image-EN.png)

## 4. Show guided reading from the page

Gemma processes the image locally to:

- understand the page;
- extract the visible text;
- simplify the reading material;
- prepare content for guided reading.

The interface then supports:

- original and simplified text;
- line-by-line reading;
- word-by-word reading;
- syllable-assisted display;
- adjustable font size, speed, and reading colors;
- audio playback.

![Simplified Guided Reading](../screenshots/EN/4-image-reading-simplified-EN.png)

## 5. Move to speech-based practice

The speech path demonstrates that DyslexAI is not limited to photographed text. The child can:

- generate a reading phrase;
- read it aloud;
- record the attempt;
- use the result as a guided reading exercise.

![Reading From Speech](../screenshots/EN/5-reading-from-speech-EN.png)

## 6. Present the feedback result

After the recording is processed, the app can show:

- the expected phrase;
- the transcribed phrase;
- syllabified reading support;
- feedback prepared for the child;
- audio playback of the relevant text.

This closes the loop from passive reading support to active reading practice.

![Speech Feedback Results](../screenshots/EN/6-results-feedback-reading-EN.png)

## What the demo proves

The walkthrough demonstrates that one local Gemma-based experience can connect:

- multimodal image understanding;
- text simplification;
- guided reading;
- syllable support;
- speech transcription;
- reading feedback;
- multilingual interaction;
- privacy-friendly local inference.

## Suggested presenter framing

A concise framing for the demo is:

> DyslexAI helps a child move from a photographed school page to a more accessible reading experience, and then from reading support into reading practice, all through one local Gemma-powered mobile app.
