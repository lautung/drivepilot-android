# DrivePilot Android

**English** · [简体中文](README.zh-CN.md)

A high-fidelity, offline smart-vehicle companion prototype built with Kotlin and Jetpack Compose. The app recreates a 16-screen vehicle control experience with local assets, Solar-style icons, persistent interactions, and no runtime network dependency.

> This project is a UI demonstration. It does not connect to a real vehicle or perform real payments, navigation, Bluetooth, camera, location, or remote-control operations.

## Screenshots

<table>
  <tr>
    <th>Home</th>
    <th>Discover</th>
    <th>Services</th>
    <th>Profile</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/home.png" width="220" alt="DrivePilot home screen" /></td>
    <td><img src="docs/screenshots/discover.png" width="220" alt="DrivePilot discover screen" /></td>
    <td><img src="docs/screenshots/services.png" width="220" alt="DrivePilot services screen" /></td>
    <td><img src="docs/screenshots/profile.png" width="220" alt="DrivePilot profile screen" /></td>
  </tr>
</table>

Screenshots were captured from MuMu Android 12 at 1080 × 1920.

## Features

- **Vehicle home:** lock control, smart cabin, sentry mode, body control, charging map, and digital key.
- **Discover:** automotive lifestyle content, intelligent-driving introduction, and official live stream.
- **Services:** vehicle configurator, maintenance booking, roadside assistance, and software subscription.
- **Profile:** user center, driving reports, service entries, account security, and privacy settings.
- **16 navigable screens:** all prototype destinations are connected through Navigation Compose.
- **Persistent demo state:** switches, sliders, dates, configurations, subscriptions, permissions, and digital-key state are stored with DataStore.
- **Offline-first presentation:** images, vectors, maps, charts, temperature controls, and demo data are packaged locally.
- **Prototype-faithful icons:** local Solar vector paths are used instead of approximate Material icons.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin 2.3.21 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.9.8 |
| State | ViewModel + immutable UI state + Kotlin Flow |
| Persistence | Preferences DataStore 1.2.1 |
| Dependency injection | Lightweight manual `AppContainer` |
| Testing | JUnit 4 + Compose UI Test + AndroidX Test |
| Android | minSdk 24, targetSdk 36 |

## Architecture

```text
app/src/main/java/com/lautung/phonecar/
├── data/
│   ├── local/          # DataStore and in-memory test state
│   └── model/          # Immutable demo models and options
├── ui/
│   ├── components/     # Shared prototype components
│   ├── navigation/     # The 16 application routes
│   ├── screens/        # Home, content, service, and profile domains
│   └── theme/          # Colors, typography, and light theme
├── AppContainer.kt     # Manual repository wiring
└── MainActivity.kt     # Single-activity Compose entry point
```

The current repositories use offline demo implementations. Their Flow-based interfaces can be replaced with real vehicle, map, content, or service backends without changing the screen contracts.

## Requirements

- Android Studio with Android SDK 36 installed
- An Android 7.0 (API 24) or newer phone/emulator
- Windows commands below use the included Gradle wrapper; on macOS/Linux, replace `.\gradlew.bat` with `./gradlew`

## Build and Install

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The generated APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Verification

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat assembleDebug
```

The connected UI suite traverses all 16 prototype screens and validates primary navigation, back-stack behavior, key controls, dialogs, and state updates.

## Project Scope

- Phone portrait layouts only
- Fixed light theme, with dark scenes retained for sentry mode and live streaming
- Local demo data; no production API credentials are required
- No location, camera, Bluetooth, network, payment, or vehicle permissions are requested
- Debug APK only; release signing is intentionally not configured

The original interaction reference is preserved in [`doc/智驾车控APP原型_v8.html`](doc/%E6%99%BA%E9%A9%BE%E8%BD%A6%E6%8E%A7APP%E5%8E%9F%E5%9E%8B_v8.html).
