package com.lift.bro.presentation.server

import android.util.Log
import com.lift.bro.di.dependencies
import com.lift.bro.di.localGoalsRepository
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.repositories.IGoalRepository
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun <T> Flow<T>.serverLogging(path: String, tag: String = "LiftBroServer") = this
    .onEach { Log.d(tag, "emitting $it $path ") }
    .onStart { Log.d(tag, "Received ws connection request to $path") }

fun Route.configureGoalRoutes(
    goalRepository: IGoalRepository = dependencies.localGoalsRepository,
) {
    webSocket("/ws/goals") {
        goalRepository.getAll()
            .serverLogging("/ws/goals")
            .onEach { goals -> send(Frame.Text(Json.encodeToString(goals))) }
            .collect()
    }
    webSocket("/ws/goal") {
        goalRepository.get(call.request.queryParameters["goalId"] ?: "")
            .serverLogging("/ws/goal")
            .onEach { goal -> send(Frame.Text(Json.encodeToString(goal))) }
            .collect()
    }

    post<Goal>("rest/goal") {
        Log.d("LiftBroServer", "Received post request to /rest/goal")
        goalRepository.save(this.call.receive())
    }

    delete("rest/goal") {
        Log.d("LiftBroServer", "Received delete request to /rest/goal")
        call.request.queryParameters["id"]?.let {
            goalRepository.delete(Goal(id = it, name = ""))
        }
    }
}
