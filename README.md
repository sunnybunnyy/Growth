# Growth

Growth is an Android plant tracking app built with Jetpack Compose. It helps users capture photos of their plants, track growth over time, and generate time-lapse videos from plant progress photos.

## What this project does

Growth provides a mobile-first experience for houseplant tracking:

- Add and manage individual plants with names, species, and dates.
- Capture a plant’s progress using the device camera.
- Store plant entries and photo history locally with Room.
- Review plant details with growth stats and a photo timeline.
- Generate and play a time-lapse video from saved plant photos.
- Use an onboarding flow that requests camera and image permissions.

## Why this project is useful

Growth is useful for developers and plant enthusiasts who want an example of:

- Jetpack Compose UI navigation and screen flow
- CameraX integration for image capture
- Local persistence with Room and Flow
- Video playback using Media3 ExoPlayer
- Time-lapse creation using saved photo assets

## Key features

- `Home` screen with a plant gallery and add button
- `Add Plant` flow with name, species, and initial photo capture
- Detailed plant view with days growing, photos count, and timeline
- Camera capture screen for new plant photos
- Time-lapse playback for plants with multiple photos
- Runtime permission handling for camera and media access

## Getting started

### Requirements

- Android Studio or compatible IDE
- Android SDK 35
- Java 11
- Connected Android device or emulator

### Clone the repository

```bash
git clone https://github.com/<your-org>/Growth.git
cd Growth
```

### Build and run

Open the project in Android Studio, then sync Gradle.

Alternatively, use the Gradle wrapper from the repository root:

```bash
./gradlew clean assembleDebug
```

Install and launch on a connected device or emulator:

```bash
./gradlew :app:installDebug
```

## Project structure

- `app/` — Android application module
- `app/src/main/java/com/example/growth/` — application source code
  - `database/` — Room database and DAO definitions
  - `model/` — data models for plants and photos
  - `ui/screens/` — Compose screens and navigation
  - `utils/` — helper utilities such as time-lapse creation
- `app/src/main/res/` — app resources, manifest, and XML assets
- `gradle/` — Gradle wrapper and dependency version catalog