# Lift Bro Client

A Kotlin Multiplatform client for communicating with the Lift Bro server. Supports both REST API calls and WebSocket connections.

## Features

- ✅ REST API calls (GET workouts, exercises, health checks)
- ✅ WebSocket real-time communication
- ✅ Cross-platform support (Android, JVM/Desktop)
- ✅ Automatic retry logic
- ✅ Configurable timeouts
- ✅ JSON serialization
- ✅ Logging support

## Usage

### Basic Setup

```kotlin
// Create a client instance
val client = createLiftBroClient()

// Configure the client (optional)
client.configure {
    enableLogging = true
    requestTimeoutMs = 10_000
    retryOnConnectionFailure = true
}

// Connect to server
client.connect("http://10.0.2.2:8080") // For Android emulator
// or
client.connect("http://localhost:8080") // For desktop/direct connection
```

### REST API Usage

```kotlin
// Get all workouts
val workoutsResult = client.getWorkouts()
workoutsResult.onSuccess { workouts ->
    println("Got ${workouts.size} workouts")
}

// Get specific workout
val workoutResult = client.getWorkout("workout-1")
workoutResult.onSuccess { workout ->
    println("Workout: ${workout.id}")
}

// Get all exercises
val exercisesResult = client.getExercises()

// Health check
val healthResult = client.getHealthCheck()
```

### WebSocket Usage

```kotlin
// Connect to WebSocket
val wsResult = client.connectWebSocket()
wsResult.onSuccess {
    println("WebSocket connected!")
}

// Listen for messages
client.getWebSocketMessages().collect { message ->
    println("Received: $message")
}

// Send a message
client.sendWebSocketMessage("Hello from client!")

// Disconnect WebSocket
client.disconnectWebSocket()
```

### Configuration Options

```kotlin
client.configure {
    connectTimeoutMs = 30_000      // Connection timeout
    requestTimeoutMs = 15_000      // Request timeout
    socketTimeoutMs = 15_000       // Socket timeout
    enableLogging = true           // Enable HTTP logging
    retryOnConnectionFailure = true // Auto-retry on failures
    maxRetryAttempts = 3           // Max retry attempts
}
```

### Cleanup

```kotlin
// Always disconnect when done
client.disconnect()
```

## Platform-Specific Notes

### Android
- Uses OkHttp engine for optimal Android performance
- For emulator testing, use `http://10.0.2.2:8080` to connect to host

### Desktop/JVM
- Uses CIO engine for lightweight operation
- Use `http://localhost:8080` for local connections

## Error Handling

All operations return `Result<T>` types for safe error handling:

```kotlin
client.getWorkouts().fold(
    onSuccess = { workouts -> 
        // Handle success
    },
    onFailure = { error ->
        // Handle error
        println("Error: ${error.message}")
    }
)
```