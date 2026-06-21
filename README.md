# DrivePilot Android

**English** · [简体中文](README.zh-CN.md)

A smart-vehicle companion prototype built with Kotlin, Jetpack Compose, and Spring Boot. The Android app retains its 16 high-fidelity screens and synchronizes accounts, simulated vehicle state, preferences, bookings, subscriptions, and discovery content with a local backend.

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
- **Accounts and sync:** username registration, login, logout, token refresh, and cached reads when offline.
- **Managed media:** admins upload private MinIO images and publish discovery content through Swagger; the app reads short-lived presigned URLs.
- **Local prototype state:** vectors, maps, charts, pending forms, and transient screen selections remain on-device.
- **Prototype-faithful icons:** local Solar vector paths are used instead of approximate Material icons.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin 2.3.21 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.9.8 |
| State | ViewModel + immutable UI state + Kotlin Flow |
| Persistence | Preferences DataStore 1.2.1 |
| Network and images | Retrofit 3 + OkHttp 5 + Kotlinx Serialization + Coil 3 |
| Dependency injection | Lightweight manual `AppContainer` |
| Testing | JUnit 4 + Compose UI Test + AndroidX Test |
| Android | minSdk 24, targetSdk 36 |
| Backend | Java 21 + Spring Boot 4.1 + PostgreSQL 16 + Flyway + MinIO |

## Architecture

```text
app/src/main/java/com/lautung/phonecar/
├── data/
│   ├── auth/           # Session and Keystore-backed refresh token
│   ├── local/          # DataStore and last successful snapshot
│   ├── model/          # Immutable UI models and options
│   ├── remote/         # Retrofit contracts and token refresh
│   └── repository/     # Local UI state and remote business repositories
├── ui/
│   ├── components/     # Shared prototype components
│   ├── navigation/     # The 16 application routes
│   ├── screens/        # Home, content, service, and profile domains
│   └── theme/          # Colors, typography, and light theme
├── AppContainer.kt     # Manual repository wiring
└── MainActivity.kt     # Single-activity Compose entry point

backend/src/main/
├── java/.../backend/   # Auth, vehicle, user, service, content, and media
└── resources/db/migration/ # Forward-only Flyway migrations
```

The server is authoritative for signed-in business state. DataStore retains the last confirmed snapshot and transient UI state. Writes require connectivity and retain the last confirmed state on failure. Vehicle controls and subscriptions are simulated only.

## Requirements

- Android Studio with Android SDK 36 installed
- An Android 7.0 (API 24) or newer phone/emulator
- Java 21, Docker Desktop, and Docker Compose
- Windows commands below use the included Gradle wrapper; on macOS/Linux, replace `.\gradlew.bat` with `./gradlew`

## Run the Local Backend

Copy `.env.example` to `.env`, replace the sample database, JWT, admin, and MinIO secrets, then run:

```powershell
docker compose up --build -d
docker compose ps
```

Swagger is at `http://localhost:8080/swagger-ui.html`, OpenAPI at `http://localhost:8080/v3/api-docs`, and the MinIO console at `http://localhost:9001`. The emulator uses `http://10.0.2.2:8080/api/v1/` by default. Override it with `-PPHONECAR_API_BASE_URL=http://host:8080/api/v1/`.

The bootstrap admin comes from `ADMIN_USERNAME` and `ADMIN_PASSWORD`; normal registration always creates `USER`. Use HTTPS, strong random secrets, and a reverse proxy before public deployment.

## Build and Install

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The generated APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Verification

```powershell
.\gradlew.bat :backend:test :backend:bootJar
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
docker compose config --quiet
```

The connected UI suite traverses all 16 prototype screens and validates primary navigation, back-stack behavior, key controls, dialogs, and state updates.

## Project Scope

- Phone portrait layouts only
- Fixed light theme, with dark scenes retained for sentry mode and live streaming
- The backend stores simulated vehicle state and demo business records only
- Android requests network access only; no location, camera, Bluetooth, payment, or real vehicle permission is requested
- Debug APK only; release signing is intentionally not configured

The original interaction reference is preserved in [`doc/智驾车控APP原型_v8.html`](doc/%E6%99%BA%E9%A9%BE%E8%BD%A6%E6%8E%A7APP%E5%8E%9F%E5%9E%8B_v8.html).
