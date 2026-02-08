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
        ignoreFailures = true
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

tasks.register("generateArchDiagram") {
    group = "documentation"
    description = "Generates a Mermaid.js diagram of the project's module dependencies."

    doLast {
        val outputFile = file("README.md")
        val content = StringBuilder("graph TD\n")

        val groups = subprojects.groupBy {
            it.group.toString().dropPrefix("Lift_Bro").dropPrefix(".").removeSuffix(".ext")
        }

        groups.forEach { (key, value) ->

            if (key.isNotBlank()) {
                content.append("  subgraph $key\n")
                value.forEach {
                    content.append("    ${key}:${it.name}\n")
                }
                content.append("  end\n")
            }
        }

        content.append("\n")

        // Loop through all modules
        subprojects.forEach { proj ->
            val id = proj.group.toString().dropPrefix("Lift_Bro").dropPrefix(".").removeSuffix(".ext")

            val moduleName = (if (id.isNotBlank()) "$id:" else "") + proj.name
            // Find dependencies in 'implementation' or 'commonMainImplementation'
            proj.configurations.forEach { config ->
                if (config.name.contains("implementation", ignoreCase = true)) {
                    config.dependencies.forEach { dep ->
                        if (dep is ProjectDependency) {
                            val group = dep.group?.dropPrefix("Lift_Bro")?.dropPrefix(".")?.removeSuffix(".ext") ?: ""
                            content.append(" $moduleName -.-> ${if (group.isNotBlank()) "$group:" else ""}${dep.name}\n")
                        }
                    }
                }
            }
        }

        val readmeContent = outputFile.readText()

        val lines = readmeContent.split("\n")
        val mermaidStart = lines.indexOf("```mermaid")
        val mermaidEnd = lines.indexOf("```")


        outputFile.writeText(
            (lines.subList(0, mermaidStart + 1) +
                content.toString().split("\n") +
                lines.subList(mermaidEnd, lines.lastIndex)).reduce
            { x, y -> x + "\n" + y }
        )
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
