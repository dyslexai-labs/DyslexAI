# DyslexAI

DyslexAI is an assistive reading application for children with dyslexia, developed in the context of the Kaggle Gemma 4 Good Hackathon.

## Project structure

- `frontend/` — Vue 3 + Vite reading interface
- `backend/` — Flask API and processing providers
- `kaggle/` — Kaggle notebook and bridge experiments with Gemma 4

## Core idea

DyslexAI helps children read school content more easily by combining:

- image upload from textbook pages
- text extraction and cleanup
- simplified text generation
- guided reading by line and by word
- visual and audio support

## Current architecture

Vue frontend -> Flask backend -> Kaggle bridge -> Gemma 4 pipeline
