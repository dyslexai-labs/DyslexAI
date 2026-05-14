# DyslexAI — Demo Flow

This document describes the recommended demo flow for the DyslexAI Android hackathon version.

---

# 1. Home Screen

DyslexAI starts with a simple guided reading interface designed for accessibility and ease of use.

The user can choose between:

- assisted reading from an image
- reading support using speech interaction

![Home Screen](../screenshots/EN/1-home-EN.png)

---

# 2. Choose Image Source

The user can select how to provide the reading content.

Available options:

- take a photo
- choose an image from the gallery

This allows the app to work with school books, worksheets and printed educational material.

![Choose Image Source](../screenshots/EN/2-choose-image-source-EN.png)

---

# 3. Confirm Image

Before processing, the user confirms that the selected image is readable.

This step helps ensure better local multimodal inference quality.

![Confirm Image](../screenshots/EN/3-confirm-image-EN.png)

---

# 4. Simplified Guided Reading

Gemma 4 local inference is used to:

- understand the image
- extract the reading content
- simplify the text
- support guided reading

The guided reading interface supports:

- simplified reading
- syllable segmentation
- line-by-line reading
- word-by-word reading
- adjustable speed
- reading colors
- audio playback

![Simplified Guided Reading](../screenshots/EN/4-image-reading-simplified-EN.png)

---

# 5. Reading From Speech

The application also supports speech-oriented reading interaction.

The user can:

- generate a phrase
- read the phrase aloud
- record speech
- prepare guided reading exercises

This demonstrates the multimodal and educational capabilities enabled by Gemma.

![Reading From Speech](../screenshots/EN/5-reading-from-speech-EN.png)

---

# 6. Speech Feedback Results

After speech processing, the app can compare:

- expected phrase
- transcribed phrase

This supports reading practice and independent learning workflows.

The interface also supports audio feedback playback.

![Speech Feedback Results](../screenshots/EN/6-results-feedback-reading-EN.png)

---

# Multimodal Gemma Capabilities

DyslexAI was designed to take advantage of multiple Gemma capabilities:

- multimodal image understanding
- text generation
- text simplification
- multilingual support
- speech-related interaction workflows
- prompt-driven task adaptation

The project demonstrates how prompt engineering and local inference can adapt Gemma to accessibility-focused educational scenarios.

---

# Final Hackathon Focus

The final version focuses on:

- Android local execution
- accessibility
- dyslexia-oriented reading support
- multilingual UI
- guided reading
- multimodal AI interaction
- privacy-friendly local inference