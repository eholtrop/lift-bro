# SwipeableNavHost - Navigation for Compose Multiplatform

A swipeable navigation component for Jetpack/Compose Multiplatform with MVI-style state management. Provides gesture-based navigation with Material 3 adaptive layout support.

## Why this library

- Swipe-based navigation with smooth animations
- Type-safe navigation using sealed classes/interfaces
- MVI pattern integration via tv.dpal:flowvi
- Material 3 adaptive layout support
- Works across Android and iOS with Compose Multiplatform
- Predictive back gesture support

## Quick start

Define your navigation destinations:

```kotlin
sealed interface Destination {
    data object Home : Destination
    data class Detail(val id: String) : Destination
}
```

Set up the navigation host:

```kotlin
@Composable
fun App() {
    val coordinator = rememberNavCoordinator(
        initialDestination = Destination.Home,
        destinations = listOf(Destination.Home)
    )
    
    SwipeableNavHost(coordinator = coordinator) { destination ->
        when (destination) {
            is Destination.Home -> HomeScreen(
                onNavigateToDetail = { id ->
                    coordinator.present(Destination.Detail(id))
                }
            )
            is Destination.Detail -> DetailScreen(
                id = destination.id,
                onBack = { coordinator.onBackPressed() }
            )
        }
    }
}
```

Navigate programmatically:

```kotlin
// Present a new screen
coordinator.present(Destination.Detail("123"))

// Navigate to existing screen in stack
coordinator.navigateTo(Destination.Home)

// Handle back navigation
coordinator.onBackPressed()

// Pop to root
coordinator.popToRoot()

// Replace entire stack
coordinator.setRoot(Destination.Home)
```

## API Overview

### NavCoordinator

The navigation controller that manages the navigation stack:

- `present(destination, animate)`: Push a new destination onto the stack
- `navigateTo(destination)`: Navigate to an existing destination in the stack
- `onBackPressed(keepStack)`: Handle back navigation
- `popToRoot(keepStack)`: Navigate to the first screen
- `setRoot(destination)`: Clear stack and set new root
- `currentPage`: Current destination
- `currentPageAsFlow`: Observe current page changes
- `pages`: List of all destinations in stack
- `pagesAsFlow`: Observe stack changes

### SwipeableNavHost

Composable that renders the current navigation destination with swipe support.

## Dependencies

This library depends on:
- `tv.dpal:flowvi` - MVI state management
- `tv.dpal:logging` - Cross-platform logging
- Compose Multiplatform
- Material 3 Adaptive layouts
- kotlinx-coroutines
- kotlinx-serialization

## Installation

Until published:

```kotlin
// settings.gradle.kts
include(":swipenavhost")

// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":swipenavhost"))
        }
    }
}
```

When published:

```kotlin
dependencies {
    implementation("tv.dpal:swipenavhost:<version>")
}
```

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
