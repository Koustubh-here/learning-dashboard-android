# Learning Dashboard (Android)

Kotlin · Jetpack Compose · MVVM · Room · Coroutines/Flow

| | |
|---|---|
| **Demo video (2 min)** | [Watch on Google Drive](PASTE_GOOGLE_DRIVE_LINK_HERE) |
| **APK** | [Download from GitHub Releases](https://github.com/Koustubh-here/learning-dashboard-android/releases/download/v1.0/learning-dashboard.apk) |

**Demo login:** `test@example.com` / `password123` (mocked API)

## 1. Architecture
MVVM with a repository layer: `Compose UI → ViewModel → Repository → Room + mock API`.
Screens only render an immutable UI state (sealed types: Loading / Empty / Error / Success) and send events up, so state flows in one direction and is easy to test. ViewModels depend on repository **interfaces**, so the fake API can be swapped for Retrofit and tests can use an in-memory fake without touching the UI. Progress is never stored; it is derived from lesson completion (`ProgressCalculator`), so the list and details screens cannot disagree. Dependencies are wired by hand in `AppContainer` to keep the scope small; I would use Hilt in production.

## 2. Offline Support
Offline-first with **Room as the single source of truth**. The UI only observes Room (`Flow`). A refresh fetches from the API and writes into Room (`@Upsert` for courses, `IGNORE` for lessons so locally completed lessons are never overwritten). If the refresh fails, the cached courses stay on screen with a "Showing saved courses" banner and a Retry action; the Error screen appears only when there is no cache. Lesson completion is saved locally and survives restarts, and a simple flag keeps the user signed in across relaunches, so the app opens offline.

## 3. Security
Store an access token and a refresh token encrypted, with the key held in the **Android Keystore** (EncryptedSharedPreferences or DataStore + Tink), never in plain SharedPreferences or logs. Use short-lived access tokens, refresh via an OkHttp authenticator, clear everything on logout, and exclude app data from backups. The current "logged in" flag is a prototype stand-in for this.

## 4. Scale (1M users, hundreds of courses)
1. **Pagination** with Paging 3 + RemoteMediator instead of loading all courses.
2. **Delta sync** (ETag / `updatedAt`) so only changed courses are downloaded.
3. **Offline write queue**: persist lesson completions and upload with WorkManager (retry, backoff, idempotent requests, server-side conflict rules).
4. **Server-side**: CDN-cached course catalog, per-user progress endpoint, rate limiting.
5. **Observability and structure**: crash reporting, analytics and tracing; DB indexes; feature modules and Hilt for build speed and team scale.

## 5. Second Platform (iOS)
SwiftUI with the same layering: `View → @Observable ViewModel → Repository protocol → SwiftData (or Core Data) + URLSession (async/await)`. The same sealed UI state maps to a Swift enum with associated values. Room's `Flow` maps to SwiftData's `@Query` or an `AsyncStream`; navigation uses `NavigationStack`; tokens go in the **Keychain**. The offline-first rule (UI reads only from the local store; network refreshes it) stays identical.

## Testing and debugging approach
Run: `./gradlew testDebugUnitTest`

Tests cover the progress calculation, the dashboard state rules (including "cached courses still shown when refresh fails"), validators, and ViewModel flows against a fake repository (completing and undoing a lesson updates progress). To debug, I modelled failures as `Result.failure` and explicit UI states, so each path (loading, empty, error, offline) can be reproduced and asserted without a device; I verified offline behaviour on the emulator with airplane mode.
