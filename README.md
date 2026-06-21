# DrivePilot Android

**English** · [简体中文](README.zh-CN.md)

A smart-vehicle companion prototype built with Kotlin, Jetpack Compose, and Spring Boot. The repository uses two independent projects in one Git repository: Android and backend own separate Gradle wrappers, dependency catalogs, and build lifecycles, and communicate only through `/api/v1` HTTP/JSON.

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
- **Accounts and sync:** registration, login, logout, token refresh, and cached reads when offline.
- **Web Admin:** the Vue admin supports secure sign-in, service overview, media upload, and the full discovery-content lifecycle; the public Viewer account remains read-only.
- **Prototype-faithful resources:** local Solar vector paths and reference images are used instead of approximate Material icons.

## Tech Stack

| Area | Technology |
| --- | --- |
| Android | Kotlin 2.3.21, Jetpack Compose, Material 3, Navigation Compose |
| State and persistence | ViewModel, Kotlin Flow, Preferences DataStore 1.2.1 |
| Network and images | Retrofit 3, OkHttp 5, Kotlinx Serialization, Coil 3 |
| Android testing | JUnit 4, Compose UI Test, AndroidX Test |
| Android versions | minSdk 24, targetSdk 36 |
| Backend | Java 21, Spring Boot 4.1, PostgreSQL 16, Flyway, MinIO |
| Web Admin | Vue 3, TypeScript, Vite, Element Plus, Pinia, TanStack Vue Query |
| Web testing | Vitest, Vue Testing Library, MSW, Playwright |

## Repository Layout

```text
apps/android/                    # Independent Android Gradle project
├── app/src/main/java/com/lautung/phonecar/
│   ├── data/                    # auth, local, model, remote, repository
│   └── ui/                      # components, navigation, screens, theme
├── gradle/libs.versions.toml    # Android-only dependencies
├── settings.gradle.kts         # Includes only :app
└── gradlew.bat

apps/admin-web/                  # Independent Vue 3 admin SPA
├── src/features/               # auth, dashboard, contents, media
├── src/shared/api/             # OpenAPI-generated types and request boundary
├── e2e/                        # Admin/Viewer Playwright flows
└── package.json

services/backend/                # Independent Spring Boot Gradle project
├── src/main/java/.../backend/   # auth, vehicle, user, service, content, media
├── src/main/resources/db/migration/
├── gradle/libs.versions.toml    # Backend-only dependencies
├── Dockerfile
└── gradlew.bat

infra/                           # Docker Compose and environment example
docs/                            # Screenshots, prototype, and design documents
tools/                           # Repository-level PowerShell tools
```

The server is authoritative for signed-in business state. DataStore retains the last confirmed snapshot and local UI state. Writes require connectivity and retain the last confirmed server state on failure.

## Requirements

- PowerShell 7 on Windows
- Android Studio, Android SDK 36, and an Android 7.0 (API 24) or newer device
- Java 21, Docker Desktop, and Docker Compose
- Node.js 22+ and pnpm 10+
- Android and backend use the wrapper inside their own directory; on macOS/Linux, replace `gradlew.bat` with `./gradlew`

## Run the Local Backend

Copy the environment example and replace the sample database, JWT, admin, read-only viewer, and MinIO secrets:

```powershell
Copy-Item infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/docker-compose.yml up --build -d
docker compose --env-file infra/.env -f infra/docker-compose.yml ps
```

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- MinIO Console: `http://localhost:9001`

The emulator uses `http://10.0.2.2:8080/api/v1/` by default. Override it for another host:

```powershell
Set-Location apps/android
.\gradlew.bat assembleDebug -PPHONECAR_API_BASE_URL=http://192.168.1.10:8080/api/v1/
```

The bootstrap admin comes from `ADMIN_USERNAME`/`ADMIN_PASSWORD`; the read-only Admin demo account comes from `VIEWER_USERNAME`/`VIEWER_PASSWORD`. Normal registration always creates `USER`. Android keeps its JSON refresh-token contract. Web Admin uses `/api/v1/auth/admin/*`: the access token remains in memory and the refresh token uses an HttpOnly cookie. MinIO credentials are never sent to clients. The `prod` profile rejects development secrets, insecure Admin cookies, and non-HTTPS public media endpoints.

## Run Web Admin

Start the local backend first, then run the Vite development server:

```powershell
Set-Location apps/admin-web
pnpm install --frozen-lockfile
pnpm dev
```

Open `http://localhost:5173`. Vite proxies `/api` and `/actuator` to `http://localhost:8080` on the same origin.

## Build and Install Android

Run `adb devices` before launching the app or device tests. Reuse a running emulator; only start or create an AVD when none is available.

```powershell
Set-Location apps/android
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The APK is located at `apps/android/app/build/outputs/apk/debug/app-debug.apk`.

## Build the Backend Independently

```powershell
Set-Location services/backend
.\gradlew.bat test bootJar
```

The Jar is located at `services/backend/build/libs/phonecar-backend.jar`.

## Verification

Run repository-level verification from the root:

```powershell
pwsh -File .\tools\verify-all.ps1
pwsh -File .\tools\verify-all.ps1 -IncludeDeviceTests
pwsh -File .\tools\verify-all.ps1 -IncludeWebE2E
```

When an API change is intentional, review and update the committed OpenAPI snapshot explicitly:

```powershell
Push-Location services/backend
.\gradlew.bat '-Dphonecar.updateOpenApiSnapshot=true' test --tests '*OpenApiContractTest'
Pop-Location
```

Or run each project separately:

```powershell
Push-Location apps/android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest
Pop-Location

Push-Location services/backend
.\gradlew.bat test bootJar
Pop-Location

Push-Location apps/admin-web
pnpm lint
pnpm format:check
pnpm typecheck
pnpm test --run
pnpm api:check
pnpm build
Pop-Location

docker compose -f infra/docker-compose.yml config --quiet
```

The connected UI suite traverses all 16 prototype screens and validates primary navigation, back-stack behavior, key controls, dialogs, and state updates.

## Project Scope

- Phone portrait layouts only, with a fixed light theme and selected dark scenes
- Simulated vehicle state and demo business records only
- No Android location, camera, Bluetooth, payment, or real vehicle permissions
- Debug APK only; release signing is intentionally not configured
- No committed `.env`, secrets, runtime state, temporary files, or build output

The original interaction reference is preserved in [`docs/prototype/智驾车控APP原型_v8.html`](docs/prototype/%E6%99%BA%E9%A9%BE%E8%BD%A6%E6%8E%A7APP%E5%8E%9F%E5%9E%8B_v8.html).
