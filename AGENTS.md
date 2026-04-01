# AGENTS.md - Lift Bro Development Guide

## Project Overview

Lift Bro is a Kotlin Multiplatform app using Jetpack Compose for Android and iOS. It tracks gym workouts with a custom MVI architecture (no ViewModels) and custom dependency injection.

## Build Commands

### Common Tasks
```bash
# Build all modules
./gradlew build

# Build debug APK only
./gradlew assembleDebug

# Clean build
./gradlew clean
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :domain:test
./gradlew :presentation:compose:test

# Run a single test class
./gradlew :domain:test --tests "com.lift.bro.domain.models.LBSetTest"

# Run a single test method
./gradlew :domain:test --tests "com.lift.bro.domain.models.LBSetTest.calculateMax tests"

# Run screenshot tests (validation)
./gradlew validateScreenshotTests

# Update screenshot tests
./gradlew updateScreenshotTests
```

### Linting & Formatting
```bash
# Run ktlint check (fail on issues)
./gradlew ktlintCheck

# Auto-fix lint issues
./gradlew ktlintFormat

# Run ktlint on specific module
./gradlew :domain:ktlintCheck
./gradlew :presentation:compose:ktlintFormat
```

## Architecture

### Module Structure
- **app-android**: Android app entry point
- **domain**: Domain models and repository interfaces (dependency-free)
- **data**: Repository implementations, SQLDelight database, Ktor client
- **presentation**: UI Composables, MVI interactors (flowvi)
- **libs**: Shared libraries (logging, navi, ext)

### MVI Pattern (flowvi)
- State: Immutable data classes with `@Serializable` for persistence
- Events: Sealed interfaces for user actions
- Interactors: `Interactor<State, Event>` with `rememberInteractor()`
- SideEffects: Handle navigation and async operations

Example structure:
```kotlin
@Serializable
data class MyState(val value: String = "")

sealed interface MyEvent {
    data class ValueChanged(val value: String): MyEvent
    data object SubmitClicked: MyEvent
}

@Composable
fun rememberMyInteractor(): Interactor<MyState, MyEvent> = rememberInteractor(
    initialState = MyState(),
    sideEffects = listOf(
        SideEffect { state, event -> /* handle events */ }
    )
)
```

### Dependency Injection
Custom static injection via `DependencyContainer`:
```kotlin
object DependencyContainer {
    // Lazy singletons
    val liftRepository: ILiftRepository by lazy { LiftRepository(...) }
    
    // JIT via get()
    fun createSetRepository(db: LBDatabase) = SetRepository(db)
}
```

## Code Style Guidelines

### Naming Conventions
- **Classes/Interfaces**: `PascalCase` (e.g., `LiftDetailsInteractor`)
- **Functions/Properties**: `camelCase` (e.g., `calculateMax`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MER_DENOMINATOR`)
- **Packages**: lowercase (e.g., `com.lift.bro.domain.models`)

### File Organization
- One class/interface per file (filename matches class name)
- Group related files in packages by feature
- Test files in `src/commonTest/kotlin/` mirror main source structure

### Imports
- Explicit imports required (no wildcard except `java.util.*`)
- Group: standard library → external → internal
- Sort alphabetically within groups

### Formatting (ktlint enforced)
- **Max line length**: 120 characters
- **Indent**: 4 spaces (no tabs)
- **No trailing whitespace**
- **Newline at end of file**

### Complexity Limits

ktlint focuses only on formatting. The following are advisory guidelines (not enforced):
- Max functions per class/interface/object: 11
- Max cyclomatic complexity: 15
- Max nested block depth: 4
- Max parameters: 6 (ignore default, data classes)
- Max return statements: 2

### Error Handling
- Use specific exception types (no generic `Exception`, `RuntimeException`, `Throwable`)
- Never swallow exceptions silently (use `ignoredExceptionTypes` pattern)
- Provide meaningful messages in exceptions

### Type Safety
- Avoid `!!` operator (prefer safe calls or `requireNotNull`)
- Use `?` for nullable types
- Prefer `val` over `var`
- Use data classes for immutable models

### Kotlin Specific
- Use extension functions for domain-specific behavior
- Use `@Composable` annotation for all Compose UI
- Mark state classes as `@Serializable` for persistence
- Use `when` expressions exhaustively

## Testing Conventions

### Test Structure
```kotlin
class MyClassTest {
    @Test
    fun `Given condition When action Then result`() {
        // Arrange
        val input = ...
        
        // Act
        val result = functionUnderTest(input)
        
        // Assert
        assertEquals(expected, result)
    }
}
```

### Test Dependencies
- `kotlin.test` - testing framework
- `kotlinx.coroutines.test` - coroutine testing
- `turbine` - Flow testing

## ktlint Configuration

ktlint handles formatting rules automatically via `.editorconfig`. It also enforces:
- No wildcard imports (except `java.util.*`)
- No `TODO:`, `FIXME:`, `STOPSHIP:` comments in code

## Common Issues

- **ktlint failures**: Run `./gradlew ktlintFormat` to auto-fix
- **Missing tests**: Add tests in `src/commonTest/kotlin/`
- **Multiplatform issues**: Platform-specific code goes in `androidMain`, `nativeMain`, etc.

## Environment Variables (Build/Release)
- `LIFT_BRO_ADMOB_APP_ID`
- `LIFT_BRO_AD_UNIT_ID`
- `LIFT_BRO_SENTRY_DSN`
- `REVENUE_CAT_API_KEY`
- `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (Android signing)
