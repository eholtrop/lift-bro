# Lift Bro Server

A Ktor-based REST API server that exposes the Lift Bro domain models via HTTP endpoints.

## Running the Server

To start the server, run:

```bash
./gradlew :presentation:server:run
```

The server will start on `http://localhost:8080`

## Available Endpoints

### Health Check
- `GET /api/health` - Returns server health status

### Workouts
- `GET /api/workouts` - Get all workouts
- `GET /api/workouts/{id}` - Get a specific workout by ID

### Exercises  
- `GET /api/exercises` - Get all exercises
- `GET /api/exercises/{id}` - Get a specific exercise by ID

### Other Endpoints
- `GET /api/lifts` - Lifts endpoint (placeholder)
- `GET /api/variations` - Variations endpoint (placeholder)  
- `GET /api/sets` - Sets endpoint (placeholder)

## Development

This is a basic setup with mock data. To extend functionality:

1. Integrate with the repository layer from the domain module
2. Add POST/PUT/DELETE endpoints for CRUD operations
3. Add authentication and authorization
4. Add input validation and error handling
5. Add database integration through the data layer

## Technology Stack

- **Ktor** - Web framework
- **Kotlin Multiplatform** - Language and platform
- **Kotlinx Serialization** - JSON serialization
- **Kotlinx Coroutines** - Async programming