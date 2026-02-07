package com.lift.bro.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
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

        val fileSpec = FileSpec.builder("com.lift.bro", "LiftBroScreenshots")
            .addType(
                TypeSpec.classBuilder("LiftBroScreenshots")
                    .addFunctions(
                        functions.map { function ->
                            val functionName = function.simpleName.asString()
                            FunSpec.builder(functionName)
                                .addAnnotation(
                                    AnnotationSpec.builder(
                                        ClassName("androidx.compose.ui.tooling.preview", "Preview")
                                    ).build()
                                )
                                .addAnnotation(
                                    AnnotationSpec.builder(
                                        ClassName("com.android.tools.screenshot", "PreviewTest")
                                    ).build()
                                )
                                .addStatement("%T()", ClassName(function.packageName.asString(), functionName))
                                .build()
                        }.asIterable()
                    )
                    .build()
            )
            .build()

        try {
            fileSpec.writeTo(environment.codeGenerator, false)
        } catch (e: IOException) {
            environment.logger.error("Error writing screenshot test file: ${e.message}")
        }

        return emptyList()
    }
}
