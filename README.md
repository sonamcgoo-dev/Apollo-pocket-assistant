# Apollo-pocket-assistant

Android native power-user assistant with a vaporwave terminal-style UI.

## Current UI direction

- Apollo bust launcher icon and startup centerpiece
- Startup ASCII art is generated per app start with randomized vaporwave variants
- Randomized Kanji + checkerboard motifs are used as vaporwave accents
- Non-essential UI animations were removed in favor of static rendering

## Build, lint, and test

From `/home/runner/work/Apollo-pocket-assistant/Apollo-pocket-assistant`:

- Build debug APK: `./gradlew :app:assembleDebug`
- Run lint: `./gradlew :app:lint`
- Run unit tests: `./gradlew :app:testDebugUnitTest`

GitHub Actions support is configured in `/home/runner/work/Apollo-pocket-assistant/Apollo-pocket-assistant/.github/workflows/android-ci.yml`, which sets up JDK 17 and the Android SDK before running the same Gradle tasks in CI.

## Environment caveat

Android Gradle Plugin artifacts are hosted on Google Maven.  
If your environment cannot resolve `dl.google.com`, Gradle fails early while resolving:

- `com.android.application` plugin (currently `8.2.2`)

In that case, verify DNS/network policy allows Google Maven access before running Gradle tasks.
