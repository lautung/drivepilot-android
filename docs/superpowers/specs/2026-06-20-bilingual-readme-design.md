# Bilingual README Design

## Goal

Present DrivePilot Android clearly to both English- and Chinese-speaking GitHub visitors, using real emulator screenshots and repository-accurate setup instructions.

## Deliverables

- `README.md` as the English landing page.
- `README.zh-CN.md` as the complete Simplified Chinese version.
- Reciprocal language links at the top of both files.
- Four MuMu Android 12 screenshots for Home, Discover, Services, and Profile under `docs/screenshots/`.

## Content Structure

Both README files use the same structure:

1. Project title, concise positioning, and language switch.
2. Screenshot gallery showing the four primary navigation destinations.
3. Feature overview covering the 16 prototype screens and persistent local interactions.
4. Technical overview covering Kotlin, Jetpack Compose, Navigation Compose, Material 3, DataStore, ViewModel, and offline assets.
5. Project structure and Android requirements.
6. Build, unit test, lint, instrumentation test, and APK output commands.
7. Scope disclaimer stating that the repository is an offline UI prototype and does not execute real vehicle control, payment, location, camera, Bluetooth, or networking operations.

## Screenshot Rules

- Capture screenshots from the connected MuMu Android 12 emulator at 1080 x 1920.
- Derive all navigation tap coordinates from the Android UI hierarchy.
- Store PNG files with stable English names: `home.png`, `discover.png`, `services.png`, and `profile.png`.
- Display images in a compact four-column HTML table so the GitHub page remains scannable.

## Repository Hygiene

- Keep generated build reports and APK files out of version control.
- Commit only the two README files, the four screenshots, and this design document.
- Preserve unrelated `.idea` files in their existing local state.

## Validation

- Confirm every relative link and screenshot path resolves locally.
- Confirm all commands match the Gradle tasks used by the project.
- Confirm both language versions contain equivalent technical facts.
- Review the staged diff before committing and pushing to GitHub.
