# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Project overview
- Kotlin Multiplatform + Jetpack Compose Multiplatform app with Android and iOS targets
- Clean architecture with three layers: presentation, domain, data
- Modules: app-android (Android app), app-compose (KMP UI/core), presentation (UI layer), domain (models/use-cases), data:sqldelight (DB + repositories)
- Build system: Gradle (Kotlin DSL) with version catalog, Gradle Wrapper committed

Commands

Gradle wrapper is committed; always use it.
- Build everything
  - ./gradlew build
- Clean
  - ./gradlew clean

Android
- Assemble Debug APK
  - ./gradlew :app-android:assembleDebug
- Install Debug on a connected device/emulator
  - ./gradlew :app-android:installDebug
- Start the app activity after install (optional)
  - adb shell am start -n com.lift.bro/.android.MainActivity
- Assemble Release (requires signing env vars; see “Config and secrets”)
  - ./gradlew :app-android:assembleRelease

iOS (KMP frameworks)
- Build iOS simulator framework (example target)
  - ./gradlew :app-compose:linkDebugFrameworkIosSimulatorArm64
- Build iOS device framework (example target)
  - ./gradlew :app-compose:linkReleaseFrameworkIosArm64
Note: Use ./gradlew :app-compose:tasks | grep -i framework to discover exact variants.

Lint
- Detekt (applied in app-android and app-compose)
  - ./gradlew :app-android:detekt :app-compose:detekt

Tests
- Unit tests per module (Android/JVM unit tests)
  - ./gradlew :domain:testDebugUnitTest
  - ./gradlew :presentation:testDebugUnitTest
  - ./gradlew :app-compose:testDebugUnitTest
  - ./gradlew :data:sqldelight:testDebugUnitTest
- Multiplatform aggregated tests (if available per module)
  - ./gradlew :domain:allTests
  - ./gradlew :presentation:allTests
  - ./gradlew :app-compose:allTests
  - ./gradlew :data:sqldelight:allTests
- Run a single unit test (example)
  - ./gradlew :presentation:testDebugUnitTest --tests "com.lift.bro.SomeTestClass.someTest"

UI tests (Maestro)
- Run all flows
  - maestro test .maestro
- Run a single flow (replace file)
  - maestro test .maestro/onboarding_tests.yaml

Fastlane (if installed)
- Android lanes (see fastlane/README.md)
  - bundle exec fastlane android build_release
  - bundle exec fastlane android test
  - bundle exec fastlane android lint
  - bundle exec fastlane android deploy
  - bundle exec fastlane android promote_internal_to_prod
- iOS lanes (manual flow currently)
  - bundle exec fastlane ios build_debug
  - bundle exec fastlane ios build_release
  - bundle exec fastlane ios test
  - bundle exec fastlane ios beta
  - bundle exec fastlane ios deploy

CI strategy
- Orchestration: GitHub Actions triggers workflows (e.g., on push, pull_request, or merges) and prepares the runner (JDK/Gradle, Android tooling) while exporting required environment variables from repository secrets.
- Delegation to Fastlane: Workflows call Fastlane lanes to define what happens (build, tests, lint, deploy). Typical lanes:
  - Android: build_release, test, lint, deploy, promote_internal_to_prod
  - iOS: build_debug/build_release, test, beta, deploy
- Secrets and config: Provide BuildKonfig/env values and (for Android release) signing secrets via GitHub Actions secrets (see Local configuration and Configuration and runtime constants sections for the exact variable names).
- Local parity: The same lanes can be invoked locally via the Fastlane commands listed above.

High-level architecture and structure

Clean layers and modules
- presentation (module: presentation)
  - Compose Multiplatform UI, navigation, and screen interactors
  - Depends on domain; maps domain models to UI models and handles UI-centric logic
  - Key packages: com.lift.bro.presentation.* (screens, navigation, interactors)
- domain (module: domain)
  - Pure Kotlin, dependency-free domain models and use-cases
  - Repository interfaces define data access contracts for presentation and data layers
  - Notable: buildkonfig exposes constants at runtime (BuildKonfig), populated from env/Gradle properties
- data (module: data:sqldelight)
  - SQLDelight schema/migrations and repository implementations of domain interfaces
  - Cross-platform drivers: Android and Native drivers are used per target
  - Coordinates mapping from database entities to domain models
- presentation (surface module)
  - This is the primary module consumed by apps (e.g., :app-android). It re-exports the internal app-compose module and owns UI, navigation, and interactors.
- app-compose (internal to presentation)
  - KMP UI implementation details, shared resources (compose.resources), framework packaging for iOS, and platform-specific source sets (androidMain, iosMain).
- app-android (module: app-android)
  - Android application packaging and entry point (AndroidManifest, MainActivity)
  - Integrates AdMob, billing, and sets up platform-specific composition locals
- buildSrc
  - Versioning utilities used by Gradle builds
  - versionCode(): derives from -PbuildNumber, offset by +156; versionName(): current date yyyy.MM.dd

Data and storage
- Database: SQLDelight (data/sqldelight)
  - Migrations under data/sqldelight/src/commonMain/sqldelight/migrations
  - Generated DB interfaces are provided by the data module and consumed by presentation/app

Configuration and runtime constants
- BuildKonfig (domain module) injects constants from Gradle properties or environment variables
  - com.lift.bro.core.buildconfig.BuildKonfig fields include: ADMOB_APP_ID, ADMOB_AD_UNIT_ID, SENTRY_DSN, REVENUE_CAT_API_KEY_AND, REVENUE_CAT_API_KEY_IOS, VERSION_NAME
- app-android build config expects:
  - LIFT_BRO_ADMOB_APP_ID (also exported to @string/admob_app_id)
  - Release signing via env vars: STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD

Notable libraries and services
- Jetpack Compose Multiplatform UI across modules
- SQLDelight for DB and migrations
- RevenueCat KMP (entitlements, paywalls) and Firebase (initialized in non-debug)
- Sentry KMP for error reporting (non-debug)
- Maestro for end-to-end UI flows

Navigation and app composition
- Navigation implemented in presentation module via a custom NavCoordinator and SwipeableNavHost
- CompositionLocal usage for shared UI state (e.g., LocalLiftBro, LocalUnitOfMeasure, subscription/purchase state)
- DI is hand-rolled via com.lift.bro.di.* (no standard DI framework); dependencies are set at app startup

Release and CI notes (from README.md)
- Android: pushed to Google Play Open Testing when code is merged to main; promoted to production on Saturdays
- iOS: currently manual; goal is nightly TestFlight with weekly promotion

Additional references
- Primary entry points
  - Android: app-android/src/main/java/com/lift/bro/android/MainActivity.kt
  - Shared app root Composable: app-compose/src/commonMain/kotlin/com/lift/bro/presentation/App.kt
- Version catalog: gradle/libs.versions.toml (Compose, Kotlin, AGP, SQLDelight, etc.)
- Maestro test flows: .maestro/

Notes
- No CLAUDE.md, Cursor rules, or Copilot rules were found in this repository at the time of writing.
