package com.lift.bro.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
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
    private var hasProcessed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (hasProcessed) {
            return emptyList()
        }

        val previewFunctions = getPreviewFunctions(resolver)
        if (previewFunctions.isNotEmpty()) {
            generateScreenshotTests(previewFunctions, resolver)
        }
        hasProcessed = true
        return emptyList()
    }

    private fun getPreviewFunctions(resolver: Resolver): List<KSFunctionDeclaration> {
        val annotationName = "androidx.compose.ui.tooling.preview.Preview"
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        return symbols.filterIsInstance<KSFunctionDeclaration>().toList()
    }

    private fun generateScreenshotTests(
        functions: List<KSFunctionDeclaration>,
        resolver: Resolver,
    ) {
        val packageName = "com.lift.bro"
        val fileName = "ScreenshotTests"

        val allProviderClasses = mutableSetOf<Pair<String, String>>()

        val providerClassesInModule = mutableMapOf<String, String>()
        resolver.getAllFiles().forEach { file ->
            file.declarations.forEach { decl ->
                if (decl is com.google.devtools.ksp.symbol.KSClassDeclaration) {
                    val fqName = decl.qualifiedName?.asString()
                    if (fqName != null) {
                        val simpleName = fqName.substringAfterLast(".")
                        providerClassesInModule[simpleName] = fqName
                    }
                }
            }
        }

        val functionsWithParams = functions.map { function ->
            val funcPackage = function.containingFile?.packageName?.asString() ?: ""
            val previewParameters = function.parameters.filter { param ->
                param.annotations.any {
                    it.annotationType.resolve().declaration.simpleName.asString() == "PreviewParameter"
                }
            }
            val paramInfoList = previewParameters.mapNotNull { param ->
                val paramType = param.type.resolve().declaration.qualifiedName?.asString()
                    ?: param.type.toString()
                val paramName = param.name?.asString() ?: return@mapNotNull null
                val previewParamAnnotation = param.annotations.find {
                    it.annotationType.resolve().declaration.simpleName.asString() == "PreviewParameter"
                }

                val providerClassInfo: String = previewParamAnnotation?.let { ann ->
                    val arg = ann.arguments.firstOrNull()
                    if (arg != null) {
                        val value = arg.value
                        when (value) {
                            is com.google.devtools.ksp.symbol.KSClassDeclaration -> {
                                value.qualifiedName?.asString()?.substringAfterLast(".")
                                    ?: "UnknownProvider"
                            }
                            else -> {
                                value.toString().substringBefore("::class").substringAfterLast(".")
                                    .ifEmpty { "UnknownProvider" }
                            }
                        }
                    } else {
                        "UnknownProvider"
                    }
                } ?: "UnknownProvider"

                Triple(paramName, paramType, providerClassInfo)
            }
            function to paramInfoList
        }

        try {
            val sources = functions.mapNotNull { it.containingFile }.toTypedArray()
            val file = environment.codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = true, sources = sources),
                packageName = packageName,
                fileName = fileName,
                extensionName = "kt",
            )

            file.use { outputStream ->
                val imports = allProviderClasses.joinToString("\n") { (fqName, _) ->
                    "import $fqName"
                }

                val commonImports = """
import com.lift.bro.utils.*
import com.lift.bro.presentation.home.*
import com.lift.bro.presentation.lift.*
import com.lift.bro.presentation.set.*
import com.lift.bro.presentation.variation.*
import com.lift.bro.presentation.workout.*
import com.lift.bro.presentation.wrapped.*
import com.lift.bro.presentation.settings.*
import com.lift.bro.presentation.onboarding.*
import com.lift.bro.ui.*
""".trimIndent()

                outputStream.write(
                    """
package $packageName

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.android.tools.screenshot.PreviewTest
import androidx.compose.runtime.Composable
$commonImports
$imports

class LiftBroScreenshots {
""".toByteArray(),
                )

                functionsWithParams.forEach { (function, paramInfoList) ->
                    generateTestFunction(outputStream, function, paramInfoList)
                }

                outputStream.write(
                    """
}
""".toByteArray(),
                )
            }
        } catch (e: IOException) {
            environment.logger.error("Error writing screenshot test file: ${e.message}")
        }
    }

    private fun generateTestFunction(
        outputStream: java.io.OutputStream,
        function: KSFunctionDeclaration,
        paramInfoList: List<Triple<String, String, String>>,
    ) {
        val functionName = function.simpleName.asString()
        val composableFunction = function.qualifiedName?.asString() ?: functionName

        if (paramInfoList.isEmpty()) {
            outputStream.write(
                """
    @Preview
    @Composable
    @PreviewTest
    fun ${functionName}Test() {
        $composableFunction()
    }
""".toByteArray(),
            )
        } else {
            val paramDeclarations = paramInfoList.joinToString(",\n") { (paramName, paramType, providerClass) ->
                "@PreviewParameter($providerClass::class) $paramName: $paramType"
            }

            val paramArguments = paramInfoList.joinToString(", ") { (paramName, _, _) ->
                paramName
            }

            outputStream.write(
                """
    @Preview
    @Composable
    @PreviewTest
    fun ${functionName}Test(
$paramDeclarations
    ) {
        $composableFunction($paramArguments)
    }
""".toByteArray(),
            )
        }
    }
}
