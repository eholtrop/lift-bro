import com.android.utils.text.dropPrefix

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.google.services) apply false

    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover)
}

kover {
    reports {
        total {
            html {
                title.set("Lift Bro Coverage")
            }
            xml {
                onCheck.set(true)
            }
            filters {
                excludes {
                    classes(
                        "*.sq.*",
                        "*.SQ*",
                        "*BuildKonfig*",
                        "*Factory*",
                        "*_Module*",
                        "*.di.*",
                    )
                }
            }
        }
    }
}

dependencies {
    kover(project(":domain"))
    kover(project(":data:core"))
    kover(project(":data:sqldelight"))
    kover(project(":data:client"))
    kover(project(":presentation:compose"))
    kover(project(":presentation:server"))
    kover(project(":libs:logging"))
    kover(project(":libs:ext:flow"))
    kover(project(":libs:ext:ktx-datetime"))
    kover(project(":libs:ext:compose"))
    kover(project(":libs:navi"))
    kover(project(":app-android"))
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        baseline = file("$projectDir/detekt-baseline.xml")

        // Auto-detect source sets based on project type
        source.setFrom(
            fileTree(projectDir) {
                include(
                    "src/**/kotlin/**/*.kt",
                    "src/**/java/**/*.kt",
                    "src/**/java/**/*.java"
                )
                exclude(
                    "**/build/**",
                    "**/resources/**",
                    "**/generated/**"
                )
            }
        )
    }

    // Create detektFormat task for each subproject with autoCorrect enabled
    tasks.register("detektFormat", io.gitlab.arturbosch.detekt.Detekt::class.java) {
        description = "Run detekt with auto-correction enabled"
        group = "formatting"

        autoCorrect = true
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true

        setSource(files(projectDir))
        include("**/*.kt", "**/*.java")
        exclude("**/build/**", "**/resources/**", "**/generated/**")
    }
}

// Aggregate detekt task that runs on all modules
tasks.register("detekt") {
    group = "verification"
    description = "Run detekt on all modules (use --continue to see all module results)"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("detekt") })
}

// Aggregate detektFormat task that runs on all modules
tasks.register("detektFormat") {
    group = "formatting"
    description = "Run detekt with auto-correction on all modules"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("detektFormat") })
}

val archBuildFiles: List<File> = subprojects.map { it.buildFile }
val archOutputFile = File(rootProject.projectDir, "README.md")
val archSettingsFile = File(rootProject.projectDir, "settings.gradle.kts")
val archVersionCatalog = File(rootProject.projectDir, "gradle/libs.versions.toml")

val generateArchTask = tasks.register<com.lift.bro.GenerateArchDiagramTask>("generateArchDiagram") {
    group = "documentation"
    description = "Generates a Mermaid.js diagram of the project's module dependencies."

    inputs.file(archSettingsFile)
    inputs.files(archBuildFiles)
    inputs.file(archVersionCatalog).optional()
    outputFile.set(archOutputFile)
}

gradle.projectsEvaluated {
    val groups = subprojects.groupBy {
        it.group.toString().dropPrefix("Lift_Bro").dropPrefix(".").removeSuffix(".ext")
    }.mapValues { (_, projects) -> projects.map { it.name } }

    val edges = subprojects.flatMap { proj ->
        val id = proj.group.toString().dropPrefix("Lift_Bro").dropPrefix(".").removeSuffix(".ext")
        val moduleName = (if (id.isNotBlank()) "$id:" else "") + proj.name
        proj.configurations.flatMap { config ->
            if (config.name.contains("implementation", ignoreCase = true)) {
                config.dependencies.filterIsInstance<ProjectDependency>().map { dep ->
                    val group = dep.group?.dropPrefix("Lift_Bro")?.dropPrefix(".")?.removeSuffix(".ext") ?: ""
                    " $moduleName -.-> ${if (group.isNotBlank()) "$group:" else ""}${dep.name}"
                }
            } else emptyList()
        }
    }

    generateArchTask.configure {
        this.groups.set(groups)
        this.edges.set(edges)
    }

    subprojects.forEach { proj ->
        proj.tasks.matching { it.name == "assemble" }.configureEach {
            finalizedBy(generateArchTask)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("enableLocalFlowvi") {
    group = "flowvi"
    doLast {
        with(file("libs/flowvi/enablecompositebuilds")) {
            parentFile.mkdirs()
            createNewFile()
        }
    }
}

tasks.register("disableLocalFlowvi") {
    group = "flowvi"
    doLast {
        with(file("libs/flowvi/enablecompositebuilds")) {
            if (exists()) delete()
        }
    }
}
