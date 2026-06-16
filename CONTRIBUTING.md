# Contributing to Mini

Thanks for your interest in improving **Mini – Minimalist Focus Launcher**. This guide explains how to get set up, propose changes, and follow our general contribution expectations so we can keep the launcher stable, minimalist, and friendly to new contributors.

## Table of Contents
1. [Code of Conduct](#code-of-conduct)
2. [Project Setup](#project-setup)
3. [Branching and Workflow](#branching-and-workflow)
4. [Development Guidelines](#development-guidelines)
5. [Testing and Validation](#testing-and-validation)
6. [Pull Request Checklist](#pull-request-checklist)
7. [Contribution Expectations](#contribution-expectations)
8. [Reporting Vulnerabilities](#reporting-vulnerabilities)
9. [Questions and Support](#questions-and-support)

## Code of Conduct
Participation in this project is governed by the [Code of Conduct](CODE_OF_CONDUCT.md). By contributing you agree to uphold a respectful, harassment-free environment and follow the enforcement process described there.

## Project Setup
1. **Prerequisites**
   - Android Studio Hedgehog (2023.1.1) or newer
   - JDK 17+
   - Android SDK platforms 29 through 35 with Google APIs
   - Gradle Wrapper (bundled)
2. **Clone and open**
   ```bash
   git clone https://github.com/A-Akhil/Minimalist-Focus-Launcher.git
   cd mini-launcher
   ```
   Open the project in Android Studio and let it sync.
3. **Build commands**
   - `./gradlew assembleDebug` (required before every pull request)
   - `./gradlew lint` (run when your change touches Kotlin/Compose/UI logic)
   - `./gradlew test` (run if you add or modify JVM unit tests)
4. **Device testing**
   - Use a device or emulator running Android 10 (API 29) or higher.
   - When testing launcher flows, make Mini the default launcher and verify you can return to the stock launcher afterward.

## Branching and Workflow
1. **Pick or file an issue**
   - Look for existing issues labeled `beginner`, `intermediate`, or `advanced` (or similar triage tags). If nothing fits, open a new issue describing the problem/idea before writing code.
   - If your idea is new, open an issue first so it can be discussed and labeled.
2. **Assignment**
   - Comment on the issue to request assignment. Maintainers will assign on a first-come, first-ready basis.
3. **Branch naming**
   - Use `feature/<short-description>` for enhancements.
   - Use `bugfix/<short-description>` for fixes.
   - Use `docs/<short-description>` for documentation-only updates.
4. **Sync often**
   - Rebase against `main` before opening or updating a pull request to avoid conflicts.

## Development Guidelines
- **Kotlin + Compose first**: follow Jetpack Compose best practices (stateless composables where possible, hoist state, prefer immutable data classes).
- **Minimalist UI**: keep backgrounds pure black (#000000) and text monochrome unless an existing component already uses accent colors.
- **No emojis**: repository-wide policy forbids emoji characters in code, UI copy, and documentation.
- **Accessibility**: provide content descriptions, respect large fonts, and keep focus order predictable.
- **Data persistence**: use Room and DataStore patterns already present in the project rather than adding new storage mechanisms.
- **Documentation**: update user-facing docs (README, changelog, in-app copy) when behavior changes; internal planning files (.md trackers in the repo root) are maintainer-only, so coordinate before touching them.

## Testing and Validation
Before requesting review:
1. Run `./gradlew assembleDebug` and ensure it succeeds without Kotlin warnings introduced by your change.
2. Run `./gradlew lint` (and `./gradlew test` if applicable).
3. Manually test launcher-critical flows affected by your change (home/tasks/app drawer navigation, onboarding, permissions, notification inbox, locks, etc.).
4. Capture screenshots or screen recordings when you modify UI.
5. Note any known limitations or follow-ups in your pull request description.

## Pull Request Checklist
Every PR must:
- Reference the issue it resolves ("Closes #123").
- Include a summary of changes, test evidence, and screenshots/GIFs for UI adjustments.
- Use the provided pull request template.
- Keep commits logical; squash or rebase as needed before review.
- Tag the appropriate labels (bug/enhancement/documentation/difficulty, etc.) so maintainers can triage quickly.
- Pass CI (if configured) and local build/lint/test checks.

## Contribution Expectations
- Keep issues labeled with an appropriate difficulty (`beginner`, `intermediate`, or `advanced`) so new contributors understand the scope.
- Only work on issues that are assigned to you or clearly unclaimed; leave a comment if you need more time or want to hand it back.
- Low-effort or spam pull requests will be closed; quality, reproducibility, and respectful discussion are required.
- Communicate early in the issue thread if you hit blockers so maintainers can help or reassign.

## Reporting Vulnerabilities
Please avoid filing public issues for security-sensitive findings. Instead, contact the maintainer privately (use the email listed on [@A-Akhil](https://github.com/A-Akhil)'s GitHub profile) so we can coordinate a fix responsibly.

## Questions and Support
- Discuss open work inside the relevant GitHub issue for transparency.
- For general questions, open a "Question" issue or reach out via the contact method in the maintainer's GitHub profile.
- When in doubt, ask—maintainers would rather help early than roll back a large change later.
