package com.lift.bro.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

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
        val functions = symbols.filterIsInstance<KSFunctionDeclaration>().toList()

        if (functions.isEmpty()) {
            return emptyList()
        }

        val packageName = "com.lift.bro"
        val fileName = "LiftBroScreenshots"

        // Collect unique full package names from all preview functions
        val uniquePackages = functions.mapNotNull { func ->
            func.qualifiedName?.asString()?.substringBeforeLast(".")
        }.distinct()

        // Types that we can't easily provide default values for
        val complexTypes = setOf(
            "State", "Workout", "Lift", "Variation", "Set",
            "Exercise", "WorkoutPlan", "Settings", "User",
            "TimerState", "TimerEvent"
        )

        // Filter out previews in timer package or previews that reference external/com.example packages
        val validFunctions = functions.filter { func ->
            val funcPackage = func.qualifiedName?.asString()?.substringBeforeLast(".") ?: ""
            val isTimerPackage = funcPackage.contains("timer")
            val referencesExternalPackage = func.packageName.asString().contains("example")
            !isTimerPackage && !referencesExternalPackage
        }

        if (validFunctions.isEmpty()) {
            environment.logger.warn("No valid preview functions to generate")
            return functions
        }

        try {
            environment.codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = false, sources = emptyArray()),
                packageName = packageName,
                fileName = fileName,
                extensionName = "kt"
            ).use { outputStream ->
                val imports = validFunctions.mapNotNull { func ->
                    func.packageName.asString().takeIf { it.isNotEmpty() }
                }.distinct().joinToString("\n") { pkg ->
                    "import $pkg.*"
                }

                outputStream.write(
                    """
package $packageName

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest

$imports

class LiftBroScreenshots {
""".toByteArray()
                )

                validFunctions.forEach { function ->
                    val testFuncName = function.simpleName.asString() + "Test"
                    val originalName = function.simpleName.asString()

                    // Check if function has parameters
                    val params = function.parameters
                    val call = if (params.isEmpty()) {
                        "$originalName()"
                    } else {
                        // Check for complex types that we can't provide
                        val hasComplexType = params.any { param ->
                            val typeName = param.type.resolve().declaration.qualifiedName?.asString() ?: ""
                            complexTypes.any { typeName.endsWith(it) }
                        }

                        if (hasComplexType) {
                            "// TODO: $originalName() has complex state types"
                        } else {
                            // Generate default values for simple parameters
                            val paramValues = params.joinToString(", ") { param ->
                                val typeName = param.type.resolve().declaration.qualifiedName?.asString() ?: "Any"
                                when {
                                    typeName.endsWith("Boolean") -> "false"
                                    typeName.endsWith("Int") -> "0"
                                    typeName.endsWith("Long") -> "0L"
                                    typeName.endsWith("Float") -> "0f"
                                    typeName.endsWith("Double") -> "0.0"
                                    typeName.endsWith("String") -> "\"\""
                                    else -> "null"
                                }
                            }
                            "$originalName($paramValues)"
                        }
                    }

                    outputStream.write(
                        """
    @PreviewTest
    @Composable
    fun $testFuncName() {
        $call
    }
""".toByteArray()
                    )
                }

                outputStream.write(
                    """
}
""".toByteArray()
                )
            }
        } catch (e: Exception) {
            environment.logger.warn("Could not generate screenshot test file: ${e.message}")
        }

        return functions
    }
}
