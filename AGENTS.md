# Lift Bro

Kotlin Multiplatform app (Android + iOS) using Jetpack Compose Multiplatform. Tracks gym workouts. Custom MVI (flowvi), no ViewModels, no Dagger.

## Modules

From `settings.gradle.kts`:
- `:app-android` — Android app entry point
- `:presentation:compose` — UI composables + interactors
- `:presentation:server` — embedded Ktor server (depends on presentation:compose)
- `:domain` — dependency-free domain models and repository interfaces
- `:data:client` — Ktor client + remote datasources
- `:data:sqldelight` — SQLDelight database
- `:data:core` — repository implementations combining local/remote
- `:libs:flowvi` — MVI library (also published as `tv.dpal:flowvi-*`)
- `:libs:navi` — custom navigation library
- `:libs:logging`, `:libs:ext:*`, `:libs:screenshot-processor`

Test files mirror source at `src/commonTest/kotlin/`.

## Key Commands

```bash
./gradlew :domain:test                                # single module tests
./gradlew :domain:test --tests "com.lift.bro.domain.models.LBSetTest"  # single class
./gradlew :domain:test --tests "com.lift.bro.domain.models.LBSetTest.calculateMax tests"  # method
./gradlew validateScreenshotTests                     # validate
./gradlew updateScreenshotTests                       # update
./gradlew detekt                                       # lint all (fails on issues)
./gradlew detektFormat                                 # auto-fix all modules
./gradlew detektFormat --continue                      # fix what you can, report rest
./gradlew generateArchDiagram                          # update README.md mermaid diagram
./gradlew :app-android:assembleDebug                   # debug APK
./gradlew clean                                         # full clean
./gradlew koverHtmlReport                               # HTML coverage report
./gradlew koverXmlReport                                # JaCoCo XML for CI
```

## Architecture

- **flowvi MVI**: `Interactor<State, Event>` + `rememberInteractor()` + `SideEffect`. State is `@Serializable`. Events are sealed interfaces or classes. Reducer functions are pure and testable independently.
- **Reducer testing pattern**: Extract pure reducer functions (e.g. `homeReducer`, `digitReducer`) and test state transitions directly. See `HomeReducerTest`, `EditLiftReducerTest`, `CalculatorInteractorTest`.
- **DI**: `expect/actual class DependencyContainer` with extension properties for JIT repository construction. Platform `actual` in `androidMain`/`nativeMain`. Accessed via `dependencies` property.
- **Repository dual-mode**: Each repository checks `settingsRepository.getClientUrl()` to choose local (SQLDelight) or remote (Ktor) datasource. `local*Repository` variants force local.
- **Navigation**: Custom lib (`:libs:navi`) through `LiftBroNavCoordinator` / `LocalNavCoordinator`.
- **BuildKonfig**: Environment variables (`LIFT_BRO_ADMOB_APP_ID`, `LIFT_BRO_SENTRY_DSN`, etc.) injected at build time via `com.codingfeline.buildkonfig`.

## Testing

- Framework: `kotlin.test` + `kotlinx.coroutines.test` (`runTest`) + `turbine` (Flow) + `mockk`
- Coverage: `kotlinx-kover` (`./gradlew koverHtmlReport`)
- Screenshot tests in `app-android/src/screenshotTest/` using `@PreviewTest` annotation
- Maestro UI tests in `.maestro/` (8 flow files) and `maestro_tests/`

### Test Doubles

Use the simplest test double that satisfies the test's intent:

| Double | When to Use | Example |
|--------|-------------|---------|
| **Stub** | Test needs a fixed return value; behavior never changes across tests | `FakeSetRepository` returning `flowOf(emptyList())` |
| **Fake** | Test needs real(ish) in-memory implementation that retains state across calls | `FakeSetDataSource` storing sets in a `MutableList` |
| **Mock** | Test needs to verify an interaction — method called with specific arguments | Verifying `analytics.trackScreenView("DASHBOARD")` on `AddLiftClicked` |
| **Spy** | Rare: wraps a real object to observe calls without replacing it | Android Intent verification (avoid in KMP) |

**Rules:**
- Prefer **Fakes** over Mocks. Fakes test behavior end-to-end; Mocks test interaction in isolation.
- Use **Mocks** only when verifying side effects (analytics, navigation, logging) where the return value is irrelevant.
- **Stubs** are acceptable for simple delegation tests where the return value is all that matters.
- **Spies** are rarely needed; avoid unless testing platform-specific interop.
- Test doubles are hand-written or use MockK. Place shared doubles in `src/commonTest/kotlin/.../testdoubles/`.
- Test doubles should be `private class` inside the test file unless shared across multiple test files.

## Localization

Strings in `presentation/compose/src/commonMain/composeResources/values/strings.xml`. Reference via `stringResource(Res.string.{name})`. Name semantically by usage location — e.g. `dashboard_toolbar_title`, not `welcome`. Every new string must also be added to all three translation files: `values-es/strings-es.xml` (Spanish), `values-fr/strings-fr.xml` (French), `values-pt/strings-pt.xml` (Portuguese). Suffix conventions: `_cta`, `_title`, `_subtitle`, `_label`, `_placeholder`, `_content_description`, `_text`, `_dialog_title`, `_paragraph_*`.

## Quirks & Gotchas

- **flowvi composite build**: To use local `libs/flowvi` source instead of published artifact, run `./gradlew enableLocalFlowvi` (creates `libs/flowvi/enablecompositebuilds`). `./gradlew disableLocalFlowvi` to revert.
- **detekt**: Each module has its own `detekt-baseline.xml`. Aggregated root tasks run on all subprojects. `buildUponDefaultConfig = true` in root `build.gradle.kts`.
- **Pre-commit hook** (`hooks/pre-commit`): Runs `generateArchDiagram` then `detektFormat --continue`. Uses SDKMAN to select Java 21.
- **iOS**: Native targets disabled on non-macOS (`kotlin.native.ignoreDisabledTargets=true`). CI uses Xcode 26 via fastlane.
- **JVM target validation**: Explicitly set to `IGNORE` in `gradle.properties`.
- **Gradle**: Configuration cache + parallel + 8G heap. Compose screenshot support enabled via `android.experimental.enableScreenshotTest=true`.
