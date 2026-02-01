plugins {
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotlin.serialization) apply false

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

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
