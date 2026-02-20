# AGENTS.md - Lift Bro Development Guide

This guide helps agentic coding agents work effectively with the Lift Bro Kotlin Multiplatform fitness tracking application.

## Project Overview

**Lift Bro** is a Kotlin Multiplatform (KMP) fitness tracking app built with Jetpack Compose Multiplatform, targeting Android and iOS. It follows Clean Architecture with MVI (Model-View-Intent) pattern for UI state management.

**Tech Stack:**
- Kotlin 2.3.0 + KMP
- Jetpack Compose Multiplatform 1.8.1
- SQLDelight 2.0.2 (database)
- Ktor 3.0.0 (networking)
- Custom dependency injection (no DI framework)

## Build & Development Commands

### Essential Commands
```bash
# Build entire project
./gradlew build

# Android development
./gradlew :app-android:assembleDebug          # Build debug APK
./gradlew :app-android:installDebug           # Install on device
./gradlew :app-android:run                    # Run on connected device

# iOS development
./gradlew :presentation:compose:linkDebugFrameworkIosSimulatorArm64

# Linting & formatting
./gradlew detekt                              # Run static analysis
./gradlew detektFormat                        # Auto-fix formatting issues
./gradlew detekt --continue                   # See all module results

# Testing
./gradlew :domain:testDebugUnitTest           # Domain layer tests
./gradlew :presentation:compose:testDebugUnitTest  # UI tests
./gradlew :domain:allTests                    # All platform tests

# UI Testing (Maestro)
maestro test .maestro                         # All UI tests
maestro test .maestro/onboarding_tests.yaml   # Specific test flow

# Documentation
./gradlew generateArchDiagram                 # Update README architecture diagram
```

### Running Single Tests
```bash
# Run specific test class
./gradlew :domain:testDebugUnitTest --tests "*WorkoutRepositoryTest"

# Run specific test method
./gradlew :domain:testDebugUnitTest --tests "*WorkoutRepositoryTest.saveWorkout*"

# Run tests with filter
./gradlew :presentation:compose:testDebugUnitTest --tests "*ReducerTest"
```

## Code Style Guidelines

### File Structure & Organization
```
module/
├── src/
│   ├── commonMain/kotlin/        # Shared KMP code
│   ├── androidMain/kotlin/       # Android-specific
│   ├── iosMain/kotlin/          # iOS-specific
│   └── commonTest/kotlin/        # Shared tests
```

### Naming Conventions
- **Classes**: PascalCase (`WorkoutRepository`, `WorkoutScreen`)
- **Functions**: camelCase (`saveWorkout`, `onExerciseClick`)
- **Variables**: camelCase (`workoutId`, `exerciseName`)
- **Constants**: SCREAMING_SNAKE_CASE (`DEFAULT_SET_COUNT`)
- **Enum entries**: PascalCase (`InProgress`, `Completed`)
- **Files**: Same as class name (`WorkoutRepository.kt`)

### Import Organization
```kotlin
// 1. Kotlin stdlib
import kotlinx.coroutines.flow.Flow

// 2. Third-party libraries
import com.benasher44.uuid.uuid4

// 3. Project modules (alphabetical)
import com.lift.bro.domain.model.Workout
import com.lift.bro.domain.repository.WorkoutRepository

// 4. Same module imports
import .ui.WorkoutState
```

### Code Style Rules
- **4-space indentation** (no tabs)
- **120-character max line length**
- **LF line endings**
- **Trim trailing whitespace**
- **Use explicit `it` parameter** in multiline lambdas
- **Prefer `val` over `var`**
- **No wildcard imports** (`import *`)

### Architecture Patterns

#### Clean Architecture Layers
1. **Domain**: Pure Kotlin, business logic, no dependencies
2. **Data**: Repository implementations, database, network
3. **Presentation**: UI, view models, navigation

#### MVI Pattern Structure
```kotlin
// State (immutable data class)
data class WorkoutState(
    val workout: Workout? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Intent (sealed class for user actions)
sealed class WorkoutIntent {
    data class LoadWorkout(val id: UUID) : WorkoutIntent()
    data class UpdateExercise(val exercise: Exercise) : WorkoutIntent()
}

// Reducer (pure function)
fun reduce(state: WorkoutState, intent: WorkoutIntent): WorkoutState {
    return when (intent) {
        is WorkoutIntent.LoadWorkout -> state.copy(isLoading = true)
        // ...
    }
}
```

#### Repository Pattern
```kotlin
// Interface in domain layer
interface WorkoutRepository {
    suspend fun getWorkout(id: UUID): Flow<Workout?>
    suspend fun saveWorkout(workout: Workout): Result<Unit>
}

// Implementation in data layer
class SqlDelightWorkoutRepository(
    private val database: LiftBroDatabase
) : WorkoutRepository {
    // Implementation
}
```

### Error Handling Guidelines
- **Use `Result<T>`** for operations that can fail
- **Prefer `null`** for optional values vs empty collections
- **Never catch `Exception`** - catch specific exceptions
- **Wrap external library exceptions** in domain exceptions

```kotlin
// Good
suspend fun saveWorkout(workout: Workout): Result<Unit> = try {
    database.insertWorkout(workout.toEntity())
    Result.success(Unit)
} catch (e: SQLException) {
    Result.failure(WorkoutSaveException("Failed to save workout", e))
}

// Bad
suspend fun saveWorkout(workout: Workout) {
    try {
        database.insertWorkout(workout.toEntity())
    } catch (e: Exception) {
        print(e.message)
    }
}
```

### Testing Guidelines

#### Test Structure (Given-When-Then)
```kotlin
@Test
fun `should load workout when id is valid`() = runTest {
    // Given
    val workout = Workout.sample()
    every { repository.getWorkout(workout.id) } returns flowOf(workout)
    
    // When
    val result = useCase.loadWorkout(workout.id)
    
    // Then
    assertEquals(workout, result.first())
}
```

#### Test Naming
- Use backticks for descriptive test names
- Structure: `should [expected behavior] when [condition]`
- Focus on behavior, not implementation details

### Compose UI Guidelines

#### Composable Structure
```kotlin
@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onIntent: (WorkoutIntent) -> Unit
) {
    when {
        state.isLoading -> LoadingIndicator()
        state.error != null -> ErrorMessage(state.error)
        else -> WorkoutContent(state.workout, onIntent)
    }
}
```

#### State Management
- **Prefer `remember`** for local state
- **Use `LaunchedEffect`** for one-time events
- **Leverage `produceState`** for external data sources

#### String Resource Abstraction
Abstract `Res.string.*` references into a data class for testability and reusability:

```kotlin
// File: EditSetScreenStrings.kt
data class EditSetScreenStrings(
    val createSetTitle: String,
    val editSetTitle: String,
    val deleteContentDescription: String,
    val extraNotesLabel: String,
    val extraNotesPlaceholder: String,
    val variationSelectorEmptyState: String,
    val timerContentDescription: String,
) {
    companion object {
        @Composable
        fun default(): EditSetScreenStrings = EditSetScreenStrings(
            createSetTitle = stringResource(Res.string.create_set_screen_title),
            editSetTitle = stringResource(Res.string.edit_set_screen_title),
            // ... other string resources
        )
    }
}
```

Usage in composable:
```kotlin
@Composable
fun EditSetScreen(
    state: EditSetState,
    onEvent: (EditSetEvent) -> Unit,
    strings: EditSetScreenStrings = EditSetScreenStrings.default(),
) {
    Text(strings.editSetTitle)
    // Use strings object instead of stringResource(Res.string.*)
}
```

### Database Guidelines (SQLDelight)

#### Schema Files
- Location: `src/commonMain/sqldelight/`
- Extension: `.sq`
- Use descriptive table and column names

```sql
CREATE TABLE Workout (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    date INTEGER NOT NULL,
    completed INTEGER NOT NULL DEFAULT 0
);
```

#### Type Safety
- Always use generated types from SQLDelight
- Avoid raw SQL queries
- Use prepared statements for parameters

### Dependency Injection Guidelines

**No DI framework** - use manual dependency injection with factories:

```kotlin
class DomainModule(
    private val dataModule: DataModule
) {
    val workoutRepository: WorkoutRepository by lazy {
        SqlDelightWorkoutRepository(dataModule.database)
    }
    
    val getWorkoutUseCase: GetWorkoutUseCase by lazy {
        GetWorkoutUseCase(workoutRepository)
    }
}
```

### Performance Guidelines

#### Coroutines
- **Use `IO` dispatcher** for database/network operations
- **Use `Main` dispatcher** for UI operations (platform-specific)
- **Prefer `Flow`** for reactive data streams
- **Use `sharedFlow`** for events, `stateFlow` for state

#### Memory Management
- **Avoid object pooling** - let Kotlin's GC handle it
- **Use `inline` classes** for type wrappers
- **Prefer immutable data structures**

### Platform-Specific Notes

#### Android
- Target SDK 36, min SDK 24
- Use Material Design 3
- Handle configuration changes properly

#### iOS
- Use iOS-style navigation patterns
- Respect platform-specific UI conventions
- Handle iOS permissions properly

### Common Pitfalls to Avoid

1. **Mixing platform-specific code in common modules**
2. **Using `!!` operator** - prefer safe calls or explicit null checks
3. **Global state** - use dependency injection
4. **Direct database access from UI** - always use repositories
5. **Ignoring coroutines cancellation** - use structured concurrency

### Before Submitting Changes

1. **Run full test suite**: `./gradlew detekt && ./gradlew test`
2. **Check for formatting issues**: `./gradlew detektFormat`
3. **Update documentation** if adding new features
4. **Run UI tests** if changing UI components
5. **Generate architecture diagram** if changing module structure

### Getting Help

- **README.md**: Project overview and setup instructions
- **CONTRIBUTING.md**: Contribution guidelines
- **WARP.md**: WARP-specific development guide
- **Check existing tests** for patterns and conventions
- **Review similar components** for implementation guidance