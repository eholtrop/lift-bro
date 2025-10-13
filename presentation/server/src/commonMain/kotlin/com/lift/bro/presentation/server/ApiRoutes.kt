package com.lift.bro.presentation.server

import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.configureLiftRoutes(
    liftRepository: ILiftRepository = dependencies.liftRepository,
) {
    webSocket("ws/lifts") {
        liftRepository.listenAll()
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }
}

fun Route.configureVariationRoutes(
    variationRepository: IVariationRepository = dependencies.variationRepository,
) {
    webSocket("ws/variations") {
        variationRepository.listenAll()
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }
}

fun Route.configureSetRoutes(
    setRepository: ISetRepository = dependencies.setRepository,
) {
    webSocket("ws/sets") {
        setRepository.listenAll()
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }
}

fun Route.configureWorkoutRoutes(
    workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
) {
    webSocket("ws/workouts") {
        workoutRepository.getAll()
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }
}

fun Route.configureApiRoutes() {
    configureLiftRoutes()
    configureWorkoutRoutes()
    configureVariationRoutes()
    configureSetRoutes()
    // Health check endpoint
    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "healthy", "timestamp" to Clock.System.now()))
    }
}
