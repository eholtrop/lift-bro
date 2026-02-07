package com.lift.bro.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.IOException

class ScreenshotProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ScreenshotProcessor(environment)
    }
}

class ScreenshotProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("androidx.compose.ui.tooling.preview.Preview")
        val functions = symbols.filterIsInstance<KSFunctionDeclaration>()

        if (functions.none()) {
            return emptyList()
        }

        val packageName = "com.lift.bro"
        val fileName = "LiftBroScreenshots"

        try {
            val file = environment.codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = false, sources = emptyArray()),
                packageName = packageName,
                fileName = fileName,
                extensionName = "kt"
            )

            file.use { outputStream ->
                outputStream.write("""
package $packageName

import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

class LiftBroScreenshots {
""".toByteArray())

                functions.forEach { function ->
                    val functionName = function.simpleName.asString() + "Test"

                    outputStream.write("""
    @Preview
    @PreviewTest
    fun $functionName() {
        // TODO: Screenshot test
    }
""".toByteArray())
                }

                outputStream.write("""
}
""".toByteArray())
            }
        } catch (e: IOException) {
            environment.logger.error("Error writing screenshot test file: ${e.message}")
        }

        return emptyList()
    }
}
