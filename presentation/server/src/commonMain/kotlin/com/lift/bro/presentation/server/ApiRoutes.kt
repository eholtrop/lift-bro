package com.lift.bro.presentation.server

import com.lift.bro.di.dependencies
import com.lift.bro.di.localLiftRepository
import com.lift.bro.di.localSetRepository
import com.lift.bro.di.localVariationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.configureLiftRoutes(
    liftRepository: ILiftRepository = dependencies.localLiftRepository,
) {
    webSocket("/ws/lifts") {
        println("LiftBroClient: Received request to /ws/lifts")
        liftRepository.listenAll()
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }

    post<Lift>("rest/lift") {
        liftRepository.save(this.call.receive())
    }

    delete("rest/lift") {
        call.request.queryParameters["id"]?.let {
            liftRepository.delete(it)
        }
    }

    delete("rest/lifts") {
        liftRepository.deleteAll()
    }
}

fun Route.configureVariationRoutes(
    variationRepository: IVariationRepository = dependencies.localVariationRepository,
) {
    webSocket("ws/variations") {
        variationRepository.listenAll(
            liftId = call.request.queryParameters["liftId"]
        )
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }

    post<Variation>("rest/variation") {
        variationRepository.save(this.call.receive())
    }

    delete("rest/variation") {
        call.request.queryParameters["id"]?.let {
            variationRepository.delete(it)
        }
    }

    delete("rest/variations") {
        variationRepository.deleteAll()
    }
}

fun Route.configureSetRoutes(
    setRepository: ISetRepository = dependencies.localSetRepository,
) {
    webSocket("ws/sets") {
        setRepository.listenAll(
            startDate = call.request.queryParameters["startDate"]?.let { LocalDate.parse(it) },
            endDate = call.request.queryParameters["endDate"]?.let { LocalDate.parse(it) },
            variationId = call.request.queryParameters["variationId"],
            limit = call.request.queryParameters["limit"]?.toLong() ?: Long.MAX_VALUE,
        )
            .onEach { lifts -> send(Frame.Text(Json.encodeToString(lifts))) }
            .collect()
    }

    post<LBSet>("rest/sets") {
        setRepository.save(this.call.receive())
    }

    delete("rest/sets") {
        val variationId = call.request.queryParameters["variationId"]
        val setId = call.request.queryParameters["id"]
        when {
            variationId != null -> setRepository.deleteAll(variationId)
            setId != null -> setRepository.delete(
                LBSet(
                    id = call.request.queryParameters["id"] ?: "",
                    variationId = "",
                )
            )
            else -> setRepository.deleteAll()
        }
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
