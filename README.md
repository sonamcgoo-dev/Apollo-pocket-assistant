# Apollo-pocket-assistant

Android native power-user assistant with a vaporwave terminal-style UI.

## Current UI direction

- Apollo bust launcher icon and startup centerpiece
- Startup ASCII art is generated per app start with randomized vaporwave variants
- Randomized Kanji + checkerboard motifs are used as vaporwave accents
- Non-essential UI animations were removed in favor of static rendering

## Documentation

| Document | Description |
|----------|-------------|
| [docs/SETUP.md](docs/SETUP.md) | Prerequisites, build, lint, and test instructions |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layer overview, package structure, technology choices |
| [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) | Branching strategy, coding conventions, PR checklist |
| [SPEC.md](SPEC.md) | Full feature specification |

## Quick start

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run lint
./gradlew :app:lint

# Run unit tests
./gradlew :app:testDebugUnitTest
```

See [docs/SETUP.md](docs/SETUP.md) for full environment setup.

## CI

GitHub Actions support is configured in `.github/workflows/android-ci.yml`, which sets up JDK 17 and the Android SDK before running the same Gradle tasks in CI.

## Environment caveat

Android Gradle Plugin artifacts are hosted on Google Maven.  
If your environment cannot resolve `dl.google.com`, Gradle fails early while resolving:

- `com.android.application` plugin (currently `8.5.2`)

In that case, verify DNS/network policy allows Google Maven access before running Gradle tasks.
