package com.lift.bro.backup.tools

import com.lift.bro.backup.Backup
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Workout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Clock

private const val SEED = 42
private const val WEEK_COUNT = 8
private const val TRAINING_DAYS = 4
private const val LOOKBACK_DAYS = 56

private val lifts = listOf(
    Category(id = "cat-squat", name = "Squat", color = 10821734794166013952uL),
    Category(id = "cat-press", name = "Press", color = 10817520509099874304uL),
    Category(id = "cat-deadlift", name = "Deadlift", color = 10838745238316408832uL),
    Category(id = "cat-shoulder", name = "Shoulder", color = 10825246914698278912uL),
    Category(id = "cat-arm", name = "Arm", color = 10832000000000000000uL),
    Category(id = "cat-back", name = "Back", color = 10836000000000000000uL),
)

private fun lift(id: String): Category = lifts.first { it.id == id }

private val movements = listOf(
    Movement(
        id = "var-back-squat",
        lift = lift("cat-squat"),
        name = "Back Squat",
        reps = 1,
        favourite = true,
        notes = "Main squat variation",
        bodyWeight = false
    ),
    Movement(
        id = "var-front-squat",
        lift = lift("cat-squat"),
        name = "Front Squat",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-goblet-squat",
        lift = lift("cat-squat"),
        name = "Goblet Squat",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-bench-press",
        lift = lift("cat-press"),
        name = "Bench Press",
        reps = 1,
        favourite = true,
        notes = "Flat bench press",
        bodyWeight = false
    ),
    Movement(
        id = "var-overhead-press",
        lift = lift("cat-press"),
        name = "Overhead Press",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-incline-bench",
        lift = lift("cat-press"),
        name = "Incline Bench Press",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-conventional-deadlift",
        lift = lift("cat-deadlift"),
        name = "Conventional Deadlift",
        reps = 1,
        favourite = true,
        notes = "Conventional stance",
        bodyWeight = false
    ),
    Movement(
        id = "var-romanian-deadlift",
        lift = lift("cat-deadlift"),
        name = "Romanian Deadlift",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-sumo-deadlift",
        lift = lift("cat-deadlift"),
        name = "Sumo Deadlift",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-lateral-raise",
        lift = lift("cat-shoulder"),
        name = "Lateral Raise",
        reps = 1,
        favourite = true,
        bodyWeight = false
    ),
    Movement(
        id = "var-face-pull",
        lift = lift("cat-shoulder"),
        name = "Face Pull",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-arnold-press",
        lift = lift("cat-shoulder"),
        name = "Arnold Press",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-barbell-curl",
        lift = lift("cat-arm"),
        name = "Barbell Curl",
        reps = 1,
        favourite = true,
        bodyWeight = false
    ),
    Movement(
        id = "var-tricep-pushdown",
        lift = lift("cat-arm"),
        name = "Tricep Pushdown",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-hammer-curl",
        lift = lift("cat-arm"),
        name = "Hammer Curl",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-barbell-row",
        lift = lift("cat-back"),
        name = "Barbell Row",
        reps = 1,
        favourite = true,
        bodyWeight = false
    ),
    Movement(
        id = "var-lat-pulldown",
        lift = lift("cat-back"),
        name = "Lat Pulldown",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
    Movement(
        id = "var-seated-cable-row",
        lift = lift("cat-back"),
        name = "Seated Cable Row",
        reps = 1,
        favourite = false,
        bodyWeight = false
    ),
)

private data class TemplateExercise(
    val variationId: String,
    val startWeight: Double,
    val endWeight: Double,
    val reps: Long,
    val tempo: Tempo,
)

private val sessionTemplates: Map<String, List<TemplateExercise>> = mapOf(
    "squat_back" to listOf(
        TemplateExercise("var-back-squat", 225.0, 265.0, 5, Tempo(down = 3, hold = 1, up = 1)),
        TemplateExercise("var-front-squat", 155.0, 185.0, 5, Tempo(down = 3, hold = 2, up = 1)),
        TemplateExercise("var-barbell-row", 115.0, 155.0, 8, Tempo(down = 2, hold = 0, up = 1)),
        TemplateExercise("var-lat-pulldown", 100.0, 135.0, 10, Tempo(down = 2, hold = 1, up = 1)),
    ),
    "press_shoulder" to listOf(
        TemplateExercise("var-bench-press", 155.0, 195.0, 5, Tempo(down = 2, hold = 1, up = 1)),
        TemplateExercise("var-overhead-press", 95.0, 125.0, 5, Tempo(down = 2, hold = 1, up = 1)),
        TemplateExercise("var-lateral-raise", 15.0, 25.0, 12, Tempo(down = 2, hold = 0, up = 1)),
        TemplateExercise("var-face-pull", 30.0, 50.0, 15, Tempo(down = 2, hold = 1, up = 1)),
    ),
    "deadlift_arm" to listOf(
        TemplateExercise("var-conventional-deadlift", 275.0, 335.0, 3, Tempo(down = 2, hold = 0, up = 1)),
        TemplateExercise("var-romanian-deadlift", 185.0, 225.0, 8, Tempo(down = 3, hold = 1, up = 1)),
        TemplateExercise("var-barbell-curl", 40.0, 60.0, 10, Tempo(down = 2, hold = 1, up = 1)),
        TemplateExercise("var-tricep-pushdown", 40.0, 60.0, 10, Tempo(down = 2, hold = 1, up = 1)),
    ),
    "back_arm_accessory" to listOf(
        TemplateExercise("var-seated-cable-row", 90.0, 130.0, 10, Tempo(down = 2, hold = 1, up = 1)),
        TemplateExercise("var-lat-pulldown", 100.0, 140.0, 10, Tempo(down = 2, hold = 1, up = 1)),
        TemplateExercise("var-hammer-curl", 25.0, 40.0, 10, Tempo(down = 2, hold = 0, up = 1)),
        TemplateExercise("var-incline-bench", 135.0, 165.0, 8, Tempo(down = 2, hold = 1, up = 1)),
    ),
)

private val warmups = listOf(
    "5 min rowing",
    "Band pull-aparts",
    "Empty bar squats",
    "Foam rolling + dynamic stretches",
    "Jump rope 3 min",
    "Arm circles + band dislocates",
    "Goblet squats with light weight",
    "Hip 90/90 stretches",
)

private val finishers: List<String?> = listOf(
    "3 rounds of pull-ups and dips",
    "Farmer's walks 3x40m",
    "Plank hold 3x45s",
    "Bodyweight lunges 2x10 each",
    "Battle ropes 3x30s",
    "Hanging leg raises 3x10",
    null,
    null,
)

private val notesPool = listOf(
    "Felt strong today",
    "Smooth reps",
    "Grinded the last rep",
    "New rep PR!",
    "Easy sets",
    "Good volume session",
    "Felt a bit tight",
    "Warm-up sets felt heavy",
    "Locked in today",
    "Solid session overall",
    "",
    "",
    "",
)

private val logNotes = listOf(
    "Great session today. Felt energized and strong.",
    "Deadlifts felt heavy but got through it.",
    "New PR on back squat! Let's go!",
    "Good volume day. Shoulders were pumped.",
    "Arms were toast after this one.",
    "Back session was solid. Feeling progress.",
    "Easy recovery day. Focused on form.",
    "Tough session but pushed through.",
    "Feeling stronger every week.",
    "Good intensity today. RPE was high.",
    "Solid session. Nutrition was on point.",
    "Fatigue from yesterday but managed good sets.",
)

private val vibes = listOf(3, 4, 4, 5, 5, 5, 4, 3, 4, 5)

private val json = Json { prettyPrint = true }

private fun <T> Random.choice(values: List<T>): T = values[nextInt(values.size)]

private class IdCounter {
    private val value = mutableMapOf<String, Int>()
    fun next(prefix: String): String {
        val n = (value[prefix] ?: 0) + 1
        value[prefix] = n
        return "$prefix-$n"
    }
}

private data class GeneratorContext(
    val timeZone: TimeZone,
    val rng: Random,
    val counter: IdCounter,
)

private data class TrainingCalendar(
    val dates: List<Pair<LocalDate, String>>,
    val totalWeeks: Int,
)

private fun buildCalendar(startDate: LocalDate, endDate: LocalDate): TrainingCalendar {
    val dates = mutableListOf<Pair<LocalDate, String>>()
    var current = startDate
    while (current.dayOfWeek != DayOfWeek.MONDAY) {
        current = current.plus(1, DateTimeUnit.DAY)
    }
    var seededWeeks = 0
    while (current <= endDate && seededWeeks < WEEK_COUNT) {
        if (current <= endDate) dates.add(current to "squat_back")
        val wed = current.plus(2, DateTimeUnit.DAY)
        if (wed <= endDate) dates.add(wed to "press_shoulder")
        val fri = current.plus(4, DateTimeUnit.DAY)
        if (fri <= endDate) dates.add(fri to "deadlift_arm")
        val sat = current.plus(5, DateTimeUnit.DAY)
        if (sat <= endDate) dates.add(sat to "back_arm_accessory")
        current = current.plus(7, DateTimeUnit.DAY)
        seededWeeks += 1
    }
    return TrainingCalendar(dates = dates, totalWeeks = maxOf(1, seededWeeks))
}

private fun buildSet(
    exercise: TemplateExercise,
    sectionId: String,
    progress: Double = 0.0,
    setIndex: Int,
    date: LocalDate,
    ctx: GeneratorContext,
): LBSet {
    val exerciseWeight = run {
        val next = (exercise.startWeight + (exercise.endWeight - exercise.startWeight) * progress)
            .let { maxOf(exercise.startWeight, it + ctx.rng.nextDouble(-5.0, 5.0)) }
        (next / 5.0).roundToInt() * 5.0
    }
    val weight = maxOf(0.0, exerciseWeight + ctx.rng.choice(listOf(0, 0, 0, -5, -10)).toDouble())
    val reps = if (setIndex >= 2) {
        maxOf(1L, exercise.reps + ctx.rng.choice(listOf(-1, 0, 0)).toLong())
    } else {
        exercise.reps
    }
    return LBSet(
        id = ctx.counter.next("set"),
        movementId = exercise.variationId,
        exerciseSectionId = sectionId,
        weight = weight,
        reps = reps,
        tempo = exercise.tempo,
        date = date.atStartOfDayIn(ctx.timeZone),
        notes = ctx.rng.choice(notesPool),
        rpe = ctx.rng.choice(listOf(6, 7, 7, 8, 8, 8, 9, 9, 10)),
        mer = 0,
    )
}

private fun buildSection(
    exercise: TemplateExercise,
    exerciseId: String,
    progress: Double,
    date: LocalDate,
    ctx: GeneratorContext,
): Section {
    val sectionId = ctx.counter.next("section")
    val movement = movements.firstOrNull { it.id == exercise.variationId }
    val setCount = ctx.rng.choice(listOf(3, 3, 4, 4, 5))
    val sets = List(setCount) { index ->
        buildSet(exercise, sectionId, progress, index, date, ctx)
    }
    return Section(
        id = sectionId,
        exerciseId = exerciseId,
        primaryMovement = movement,
        sets = sets,
        movements = listOfNotNull(movement),
    )
}

private fun buildExercise(
    exercise: TemplateExercise,
    workoutId: String,
    progress: Double,
    date: LocalDate,
    ctx: GeneratorContext,
): Exercise {
    val exerciseId = ctx.counter.next("exercise")
    val section = buildSection(exercise, exerciseId, progress, date, ctx)
    return Exercise(
        id = exerciseId,
        workoutId = workoutId,
        sections = listOf(section),
    )
}

private fun buildWorkout(
    date: LocalDate,
    sessionType: String,
    progress: Double,
    ctx: GeneratorContext,
): Workout {
    val workoutId = ctx.counter.next("workout")
    val exercises = sessionTemplates.getValue(sessionType).map { template ->
        buildExercise(template, workoutId, progress, date, ctx)
    }
    return Workout(
        id = workoutId,
        date = date,
        warmup = ctx.rng.choice(warmups),
        exercises = exercises,
        finisher = ctx.rng.choice(finishers),
    )
}

private fun generateBackup(startDate: LocalDate, endDate: LocalDate): Backup {
    val calendar = buildCalendar(startDate, endDate)
    val ctx = GeneratorContext(
        timeZone = TimeZone.currentSystemDefault(),
        rng = Random(SEED),
        counter = IdCounter(),
    )
    val workouts = calendar.dates.mapIndexed { index, (date, sessionType) ->
        val weekNum = index / TRAINING_DAYS
        val progress = weekNum.toDouble() / maxOf(1, calendar.totalWeeks - 1)
        buildWorkout(date, sessionType, progress, ctx)
    }
    val logs = calendar.dates.map { date ->
        LiftingLog(
            id = ctx.counter.next("log"),
            date = date.first,
            notes = ctx.rng.choice(logNotes),
            vibe = ctx.rng.choice(vibes),
        )
    }
    val sets = workouts.flatMap { it.exercises }.flatMap { it.sections }.flatMap { it.sets }
    return Backup(
        lifts = lifts,
        variations = movements,
        sets = sets,
        liftingLogs = logs,
        workouts = workouts,
        exercises = workouts.flatMap { it.exercises },
    )
}

private fun writeBackup(output: File, backup: Backup) {
    val encoded = json.encodeToString(backup)
    val decoded = json.decodeFromString<Backup>(encoded)
    require(decoded.lifts?.size == lifts.size) { "Lift round-trip mismatch" }
    require(decoded.variations?.size == movements.size) { "Variation round-trip mismatch" }
    require(decoded.sets.orEmpty().isNotEmpty()) { "No sets generated" }
    require(decoded.workouts.orEmpty().isNotEmpty()) { "No workouts generated" }
    require(decoded.exercises.orEmpty().isNotEmpty()) { "No exercises generated" }
    require(decoded.liftingLogs.orEmpty().isNotEmpty()) { "No lifting logs generated" }
    output.parentFile?.mkdirs()
    output.writeText(encoded)
    println("Generated ${output.absolutePath}")
    println("  Categories: ${backup.lifts.orEmpty().size}")
    println("  Variations: ${backup.variations.orEmpty().size}")
    println("  Sets: ${backup.sets.orEmpty().size}")
    println("  Workouts: ${backup.workouts.orEmpty().size}")
    println("  Exercises: ${backup.exercises.orEmpty().size}")
    println("  Lifting Logs: ${backup.liftingLogs.orEmpty().size}")
    val dates = backup.workouts.orEmpty().map { it.date }
    println("  Date range: ${dates.minOrNull()} to ${dates.maxOrNull()}")
}

fun main(args: Array<String>) {
    val outputPath = args.getOrNull(0)
        ?: System.getProperty("backupOutputPath")
        ?: File("build/generated/test-data/screenshot_test_backup.json").absolutePath

    val timeZone = TimeZone.currentSystemDefault()
    val endDate = Clock.System.todayIn(timeZone)
    val startDate = endDate.minus(LOOKBACK_DAYS, DateTimeUnit.DAY)
    writeBackup(File(outputPath), generateBackup(startDate, endDate))
}
