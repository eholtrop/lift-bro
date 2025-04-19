import com.android.build.gradle.internal.scope.publishArtifactToConfiguration
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.gradle.kotlin.dsl.*
import java.io.ByteArrayOutputStream

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
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("generateReleaseNotes") {
    doLast {
        val outputDir = file("$buildDir/generated/release_notes")
        outputDir.mkdirs()
        val outputFile = file("$outputDir/release_notes.json")

        try {
            val process =
                ProcessBuilder("git", "log", "--pretty=format:%s", "HEAD") // Adjust range as needed
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

            val output = ByteArrayOutputStream()
            val error = ByteArrayOutputStream()

            process.inputStream.copyTo(output)
            process.errorStream.copyTo(error)

            val exitCode = process.waitFor()

            if (exitCode == 0) {
                val commitMessages = output.toString(Charsets.UTF_8).trim().split('\n')
                val features = mutableListOf<String>()
                val fixes = mutableListOf<String>()
                val improvements = mutableListOf<String>()
                val other = mutableListOf<String>()

                commitMessages.forEach { commit ->
                    val parts = commit.split(": ", limit = 2)
                    if (parts.size == 2) {
                        val typeScope = parts[0]
                        val description = parts[1]
                        val typePart = typeScope.split("(")[0]
                        when (typePart) {
                            "feat" -> features.add(description)
                            "fix" -> fixes.add(description)
                            "chore", "refactor", "perf" -> improvements.add(description)
                            else -> other.add(commit)
                        }
                    } else {
                        other.add(commit)
                    }
                }

                val notes = JsonArray().apply {
                    this.add(JsonObject().apply {
                        this.add("title", JsonPrimitive("New Features!"))
                        this.add("items", JsonArray().apply {
                            features.forEach { feature ->
                                this.add(feature)
                            }
                        })
                    })

                    this.add(JsonObject().apply {
                        this.add("title", JsonPrimitive("Bug Fixes! ;)"))
                        this.add("items", JsonArray().apply {
                            fixes.forEach { feature ->
                                this.add(feature)
                            }
                        })
                    })

                    this.add(JsonObject().apply {
                        this.add("title", JsonPrimitive("Improvements"))
                        this.add("items", JsonArray().apply {
                            improvements.forEach { feature ->
                                this.add(feature)
                            }
                        })
                    })

                    this.add(JsonObject().apply {
                        this.add("title", JsonPrimitive("Other Stuff"))
                        this.add("items", JsonArray().apply {
                            other.forEach { feature ->
                                this.add(feature)
                            }
                        })
                    })
                }

                outputFile.writeText(notes.toString())
                println("Release notes JSON generated successfully at: ${outputFile.absolutePath}")

            } else {
                System.err.println("Error fetching Git log:")
                System.err.println(error.toString(Charsets.UTF_8))
            }

        } catch (e: Exception) {
            System.err.println("Error during release notes generation: ${e.message}")
            e.printStackTrace()
        }
    }
}
