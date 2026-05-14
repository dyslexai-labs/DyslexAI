#!/bin/bash
set -e

ROOT_DIR=$(pwd)

echo "== ENTER FRONTEND =="
cd frontend

echo "== BUILD WEB =="
npm install
npm run build

echo "== SYNC CAPACITOR =="
npx cap sync android

echo "== BUILD DEBUG APK =="
cd android
./gradlew assembleDebug

echo "== CREATE RELEASE FOLDER =="
mkdir -p "$ROOT_DIR/release/apk"

echo "== COPY APK =="
cp app/build/outputs/apk/debug/app-debug.apk \
"$ROOT_DIR/release/apk/DyslexAI-Hackathon-Demo-v1.0.apk"

echo "== APK READY =="

ls -lh "$ROOT_DIR/release/apk/DyslexAI-Hackathon-Demo-v1.0.apk"