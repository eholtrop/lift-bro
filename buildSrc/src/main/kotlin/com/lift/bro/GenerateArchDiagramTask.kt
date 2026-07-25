package com.lift.bro

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateArchDiagramTask : DefaultTask() {

    @get:Input
    abstract val groups: MapProperty<String, List<String>>

    @get:Input
    abstract val edges: ListProperty<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val content = StringBuilder("graph TD\n")
        groups.get().forEach { (key, names) ->
            if (key.isNotBlank()) {
                content.append("  subgraph $key\n")
                names.forEach { content.append("    $key:$it\n") }
                content.append("  end\n")
            }
        }
        content.append("\n")
        edges.get().forEach { content.append("$it\n") }

        val file = outputFile.get().asFile
        val readmeContent = file.readText()
        val startMarker = "<!-- arch-diagram-start -->"
        val endMarker = "<!-- arch-diagram-end -->"
        val startIdx = readmeContent.indexOf(startMarker)
        val endIdx = readmeContent.indexOf(endMarker)

        if (startIdx != -1 && endIdx != -1) {
            file.writeText(
                readmeContent.substring(0, startIdx + startMarker.length) +
                    "\n```mermaid\n${content}```\n" +
                    readmeContent.substring(endIdx)
            )
        }
    }
}
