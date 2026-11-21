SAMVEDA
One Device. Multiple Abilities. One Connected World.

SAMVEDA is my first Android app built in Android Studio. It is an offline-first accessibility app for blind, visually impaired, deaf, hard-of-hearing and non-speaking people. This is a working demo (Stage 1) with a basic Android UI built with Kotlin and Jetpack Compose.

Modules:
Module	-Purpose
SPARSHA	-Visual assistance
DRISHTI	-Sign communication
SHRAVAN	-Hearing assistance
SAHAYA	-Everyday independence
Features:
Working in this demo
Basic Android UI built with Kotlin and Jetpack Compose
Home screen with four large module cards and an SOS button
Dark and light themes, large text and large touch targets
Text reading from the camera (OCR, English / Latin script, runs on the device)
Text-to-speech in English, Telugu and Hindi (needs the voice installed on the phone)
Quick communication phrases and type-to-speak
Live speech captions with a conversation view and typed replies
Vibration patterns for left, right and stop (demo)
SOS: opens a pre-filled SMS to a trusted contact, or the 112 dialer (the user presses Send or Call)
Settings for language, voice speed, vibration and theme

Planned (not built yet):

Obstacle and door detection with distance estimation
Indian Sign Language (ISL) recognition and text-to-sign animation
Alarm, horn, siren and doorbell sound detection
Indian currency recognition
Person recognition (with consent and privacy safeguards)

This app is not a replacement for a cane, a guide or any safety aid. Obstacle detection is not implemented, and OCR can make mistakes. Always double-check medicine labels.

Built with:
Kotlin
Jetpack Compose (Material 3)
CameraX
ML Kit Text Recognition (on-device OCR)
Android TextToSpeech and SpeechRecognizer
Android Vibrator APIs
How to run
Open the project in Android Studio (open the folder that contains settings.gradle.kts).
Wait for Gradle sync to finish (internet needed the first time).
Connect an emulator or a phone:
Phone: turn on Developer options → USB debugging, connect by USB and tap Allow on the phone.
Emulator: create one in Tools → Device Manager.
Select the device at the top and click Run ▶.

Requirements: Android Studio (recent version), JDK 17 or 21, Android 8.0 (API 26) or higher.

Permissions and Permission	Used for
Camera	-Reading text(SPARSHA, SAHAYA)
Microphone	-Live captions(SHRAVAN)
Vibration	-Haptic feedback
Privacy:
Camera images and microphone audio are not saved or uploaded by SAMVEDA.
Settings and the trusted contact number are stored on the phone only.
SOS never sends anything automatically.
Project status:
Implemented	-UI, OCR reading, text-to-speech, live captions, haptics, SOS shortcut, settings
Partial	-Offline captions depend on the phone's language pack; OCR is Latin script only
Planned-	Obstacle detection, ISL recognition, sound detection, currency recognition
Author:
Made by SALMA and team as a first Android project.

SAMVEDA: Technology that understands abilities, not limitations.
