package com.lift.bro.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSFile
import java.io.IOException

class ScreenshotProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ScreenshotProcessor(environment)
    }
}

class ScreenshotProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }

        val symbols = resolver.getSymbolsWithAnnotation("androidx.compose.ui.tooling.preview.Preview")
        val functions = symbols.filterIsInstance<KSFunctionDeclaration>().toList()

        if (functions.isEmpty()) {
            return emptyList()
        }

        val packageName = "com.lift.bro"
        val fileName = "ScreenshotTests"

        try {
            val sources = functions.mapNotNull { it.containingFile }.toTypedArray()
            val file = environment.codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = true, sources = sources),
                packageName = packageName,
                fileName = fileName,
                extensionName = "kt"
            )

            file.use { outputStream ->
                outputStream.write("""
package $packageName

import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import androidx.compose.runtime.Composable

class LiftBroScreenshots {
""".toByteArray())

                functions.forEach { function ->
                    val functionName = function.simpleName.asString()
                    val composableFunction = function.qualifiedName?.asString() ?: functionName

                    outputStream.write("""
    @Preview
    @Composable
    @PreviewTest
    fun ${functionName}Test() {
        $composableFunction()
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

        processed = true
        return emptyList()
    }
}
