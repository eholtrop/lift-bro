package com.lift.bro.presentation.server

import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/**
 * Container for repositories required by the presentation server.
 * Pass implementations from your app's DI.
 */
data class PresentationDataSources(
    val lifts: com.lift.bro.data.core.datasource.LiftDataSource,
    val sets: com.lift.bro.data.core.datasource.SetDataSource,
    val exercises: com.lift.bro.data.core.datasource.ExerciseDataSource,
    val variations: com.lift.bro.data.core.datasource.VariationDataSource,
    val workouts: com.lift.bro.data.core.datasource.WorkoutDataSource,
    val settings: ISettingsRepository? = null,
)

@kotlinx.serialization.Serializable
private data class UnitOfMeasureDto(val uom: String)

class PresentationServer(
    private val data: PresentationDataSources,
) {
    private var engine: ApplicationEngine? = null

    fun start(port: Int = 8080) {
        if (engine != null) return
        engine = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = false
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }
                )
            }
            routing {
                get("/health") { call.respondText("ok") }

                // Build repositories from data sources
                val liftRepo = com.lift.bro.data.core.repository.LiftRepository(local = data.lifts)
                val setRepo = com.lift.bro.data.core.repository.SetRepository(local = data.sets)
                val varRepo = com.lift.bro.data.core.repository.VariationRepository(local = data.variations)
                val exRepo = com.lift.bro.data.core.repository.ExerciseRepository(local = data.exercises)
                val workoutRepo = com.lift.bro.data.core.repository.WorkoutRepository(workout = data.workouts, exercise = data.exercises)

                // Lifts
                get("/lifts") {
                    call.respond(liftRepo.getAll())
                }
                get("/lifts/{id}") {
                    val id = call.parameters["id"]
                    call.respond(liftRepo.get(id).first() ?: return@get call.respondText("Not Found", status = io.ktor.http.HttpStatusCode.NotFound))
                }
                post("/lifts") {
                    val lift = call.receive<Lift>()
                    liftRepo.save(lift)
                    call.respond(io.ktor.http.HttpStatusCode.Created)
                }
                delete("/lifts/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    liftRepo.delete(id)
                    call.respond(io.ktor.http.HttpStatusCode.NoContent)
                }

                // Workouts
                get("/workouts") {
                    val startDate = call.request.queryParameters["startDate"]?.let(LocalDate::parse)
                        ?: LocalDate.fromEpochDays(0)
                    val endDate = call.request.queryParameters["endDate"]?.let(LocalDate::parse)
                        ?: LocalDate.fromEpochDays(Int.MAX_VALUE)
                    call.respond(workoutRepo.getAll(startDate, endDate).first())
                }
                get("/workouts/by-date/{date}") {
                    val date = call.parameters["date"]?.let(LocalDate::parse)
                        ?: return@get call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    call.respond(workoutRepo.get(date).first() ?: io.ktor.http.HttpStatusCode.NotFound)
                }
                post("/workouts") {
                    val workout = call.receive<Workout>()
                    workoutRepo.save(workout)
                    call.respond(io.ktor.http.HttpStatusCode.Created)
                }
                post("/workouts/{workoutId}/exercises/{exerciseId}") {
                    val workoutId = call.parameters["workoutId"] ?: return@post call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    val exerciseId = call.parameters["exerciseId"] ?: return@post call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    workoutRepo.addExercise(workoutId, exerciseId)
                    call.respond(io.ktor.http.HttpStatusCode.Accepted)
                }
                post("/exercises/{exerciseId}/variations/{variationId}") {
                    val exerciseId = call.parameters["exerciseId"] ?: return@post call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    val variationId = call.parameters["variationId"] ?: return@post call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    workoutRepo.addVariation(exerciseId, variationId)
                    call.respond(io.ktor.http.HttpStatusCode.Accepted)
                }
                delete("/exercises/{exerciseId}") {
                    val exerciseId = call.parameters["exerciseId"] ?: return@delete call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    workoutRepo.deleteExercise(exerciseId)
                    call.respond(io.ktor.http.HttpStatusCode.NoContent)
                }
                delete("/exercise-variations/{exerciseVariationId}") {
                    val id = call.parameters["exerciseVariationId"] ?: return@delete call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    workoutRepo.removeVariation(id)
                    call.respond(io.ktor.http.HttpStatusCode.NoContent)
                }

                // Sets
                get("/sets/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                    call.respond(setRepo.listen(id).first() ?: io.ktor.http.HttpStatusCode.NotFound)
                }
                post("/sets") {
                    val set = call.receive<LBSet>()
                    setRepo.save(set)
                    call.respond(io.ktor.http.HttpStatusCode.Created)
                }
                delete("/sets") {
                    val set = call.receive<LBSet>()
                    setRepo.delete(set)
                    call.respond(io.ktor.http.HttpStatusCode.NoContent)
                }

                // Settings (optional subset)
data.settings?.let { settingsRepo ->
                    get("/settings/uom") {
                        val current = settingsRepo.getUnitOfMeasure().first()
                        call.respond(UnitOfMeasureDto(uom = current.uom.name))
                    }
                    put("/settings/uom") {
                        val dto = call.receive<UnitOfMeasureDto>()
                        val value = try { UOM.valueOf(dto.uom) } catch (e: IllegalArgumentException) { return@put call.respond(io.ktor.http.HttpStatusCode.BadRequest) }
                        settingsRepo.saveUnitOfMeasure(Settings.UnitOfWeight(value))
                        call.respond(io.ktor.http.HttpStatusCode.NoContent)
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop(graceMillis: Long = 500, timeoutMillis: Long = 1500) {
        engine?.stop(graceMillis, timeoutMillis)
        engine = null
    }
}

fun startPresentationServer(
    port: Int = 8080,
    dataSources: PresentationDataSources,
): PresentationServer {
    return PresentationServer(dataSources).also { it.start(port) }
}
