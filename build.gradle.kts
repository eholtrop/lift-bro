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

    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.register("ktlintCheck") {
    group = "verification"
    description = "Run ktlint check on all modules (use --continue to see all module results)"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("ktlintCheck") })
}

tasks.register("ktlintFormat") {
    group = "formatting"
    description = "Run ktlint with auto-correction on all modules"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("ktlintFormat") })
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
