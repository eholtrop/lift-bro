# Kotlin Extensions for Multiplatform

A collection of useful Kotlin extension functions for Kotlin Multiplatform projects, organized into focused modules for Flow, Compose, and DateTime utilities.

## Modules

### Flow Extensions (`ext:flow`)

Utilities for working with Kotlin Coroutines Flow:

- **debug()**: Debug utility that logs flow events (onStart, onEach, onComplete, catch, onEmpty)
- **mapEach()**: Transform each item in a `Flow<List<T>>`
- **filterEach()**: Filter items in a `Flow<List<T>>`
- **combine()**: Extended combine functions for 6 and 7 flows

```kotlin
import tv.dpal.ext.flow.*

myFlow
    .debug("MyFlow")
    .mapEach { it.uppercase() }
    .filterEach { it.isNotEmpty() }
```

### Compose Extensions (`ext:compose`)

Extension functions for Jetpack/Compose Multiplatform:

- **Dp.AccessibilityMinimumSize**: Standard 48.dp accessibility minimum touch target
- **CornerExt**: Corner radius utilities
- **HorizontalPaddingExt**: Horizontal padding modifiers
- **VerticalPaddingExt**: Vertical padding modifiers
- **ColorExt**: Color manipulation utilities

```kotlin
import tv.dpal.compose.*
import androidx.compose.ui.unit.Dp

val minSize = Dp.AccessibilityMinimumSize // 48.dp
```

### DateTime Extensions (`ext:ktx-datetime`)

Extensions for kotlinx-datetime:

- **DateFormatting**: Platform-specific date formatting (Android/iOS)
- **DateTimeExt**: LocalDateTime extension functions
- **InstantExt**: Instant extension functions

## Installation

Each module can be used independently. Include only what you need:

```kotlin
// settings.gradle.kts
include(":ext:flow")
include(":ext:compose")
include(":ext:ktx-datetime")

// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Use individual modules
            implementation(project(":ext:flow"))
            implementation(project(":ext:compose"))
            implementation(project(":ext:ktx-datetime"))
        }
    }
}
```

When published:

```kotlin
dependencies {
    implementation("tv.dpal:ext-flow:<version>")
    implementation("tv.dpal:ext-compose:<version>")
    implementation("tv.dpal:ext-ktx-datetime:<version>")
}
```

## Dependencies

- **ext:flow**: Requires kotlinx-coroutines and tv.dpal:logging
- **ext:compose**: Requires Compose Multiplatform
- **ext:ktx-datetime**: Requires kotlinx-datetime

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
