#!/bin/bash

echo "== BUILD WEB =="
npm run build || exit 1

echo "== SYNC CAPACITOR =="
npx cap sync android || exit 1

echo "== BUILD APK =="
cd android || exit 1
./gradlew assembleDebug || exit 1

echo "== INSTALL APK =="
adb install -r app/build/outputs/apk/debug/app-debug.apk || exit 1

echo "== START APP =="
adb shell monkey -p om.dyslexai.app 1

echo "== DONE =="

cd ..