# Contributing to Apollo Pocket Assistant

Thank you for your interest in contributing! This document explains how to get started.

## Development Environment

Follow the [Setup Guide](SETUP.md) to get a working local build before contributing.

## Branching Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable, always-buildable state |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `chore/<name>` | Build / tooling / docs changes |

Create a branch off `main`, make your changes, then open a pull request.

## Coding Conventions

- **Language:** Kotlin, following the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **Architecture:** All new features must follow the existing MVVM / Clean Architecture layers (see [ARCHITECTURE.md](ARCHITECTURE.md)).
- **Compose:** Prefer stateless composables; hoist state to the nearest ViewModel.
- **Coroutines:** Launch coroutines from `viewModelScope` in ViewModels; use `suspend` functions in repositories.
- **No magic numbers:** Extract constants to companion objects or `object` declarations.

## Commit Messages

Use the [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <short description>

feat(chat): add streaming token display
fix(llm): handle empty response from Ollama
chore(deps): bump Kotlin to 2.0.21
docs(setup): clarify SDK requirements
```

Common types: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `style`.

## Pull Request Checklist

Before submitting a PR, ensure:

- [ ] The project builds cleanly: `./gradlew :app:assembleDebug`
- [ ] Lint passes with no new warnings: `./gradlew :app:lint`
- [ ] Unit tests pass: `./gradlew :app:testDebugUnitTest`
- [ ] New features include appropriate unit tests
- [ ] Public APIs and complex logic are documented with KDoc comments
- [ ] The PR description explains *what* changed and *why*

## Reporting Bugs

Open a GitHub issue with:
1. Steps to reproduce
2. Expected behavior
3. Actual behavior
4. Android version and device model
5. Relevant logcat output (redact any personal data)

## Feature Requests

Open a GitHub issue tagged `enhancement` and describe the use case. Check existing issues first to avoid duplicates.
