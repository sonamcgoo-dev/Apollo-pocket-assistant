# Setup Guide

## Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 |
| Android SDK | API 35 (Android 15) |
| Gradle | 8.9 (managed by wrapper) |

## Cloning the Repository

```bash
git clone https://github.com/sonamcgoo-dev/Apollo-pocket-assistant.git
cd Apollo-pocket-assistant
```

## Opening in Android Studio

1. Open Android Studio → **File → Open** and select the repository root.
2. Let Gradle sync complete (this downloads all dependencies automatically).
3. If prompted about a missing SDK version, open **SDK Manager** and install **API 35**.

## Building

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK (unsigned)
./gradlew :app:assembleRelease
```

The output APK is placed at:

```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Running Lint

```bash
./gradlew :app:lint
```

HTML and XML reports are written to `app/build/reports/lint-results-debug.html`.

## Running Unit Tests

```bash
./gradlew :app:testDebugUnitTest
```

## Network Requirement

Android Gradle Plugin artifacts are hosted on **Google Maven** (`dl.google.com`). Ensure your network/DNS allows access to that domain before running any Gradle tasks.

## CI

GitHub Actions runs the same tasks automatically on every push. See `.github/workflows/android-ci.yml` for details.
