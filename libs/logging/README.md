# Cross-Platform Logging for Kotlin Multiplatform

A minimal, no-frills logging utility for Kotlin Multiplatform projects. Provides a simple `Log.d()` function that works across Android, iOS, and other KMP targets.

## Why this library

- Minimal surface area with a single logging function
- Works across Kotlin Multiplatform targets (Android, iOS, Desktop, etc.)
- Platform-specific implementations (Android LogCat, iOS/Native println)
- Zero dependencies
- Drop-in replacement for basic logging needs

## Quick start

```kotlin
import tv.dpal.logging.Log
import tv.dpal.logging.d

// Basic logging
Log.d("MyTag", "Hello, world!")

// Custom tag or use default
Log.d(message = "Using default tag")
```

## Platform implementations

- **Android**: Uses `android.util.Log.d()`
- **iOS/Native**: Uses `println()` with tag formatting

## Installation

Until this library is published, depend on it via a composite build/module include:

```kotlin
// settings.gradle.kts
include(":logging")

// build.gradle.kts of your module
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":logging"))
        }
    }
}
```

When published, you'll be able to use:

```kotlin
// build.gradle.kts
dependencies {
    implementation("tv.dpal:logging:<version>")
}
```

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
