package com.lift.bro.annotation

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

class ValuesProcessor(
    private val codeGenerator: CodeGenerator,
    @Suppress("UNUSED_PARAMETER") private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenerateValues::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        val ret = mutableListOf<KSAnnotated>()
        symbols.forEach { classDecl ->
            if (!classDecl.validate()) {
                ret.add(classDecl)
                return@forEach
            }
            generateValuesFunction(classDecl, resolver)
        }
        return ret
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateValuesFunction(classDecl: KSClassDeclaration, resolver: Resolver) {
        val packageName = classDecl.packageName.asString()
        val name = classDecl.simpleName.getShortName()
        val subclasses = classDecl.getSealedSubclasses()
        val sb = StringBuilder()
        if (packageName.isNotEmpty()) sb.append("package ").append(packageName).append("\n\n")
        sb.append("import kotlin.collections.List\n")

        val importSet = mutableSetOf<String>()
        val refList = mutableListOf<String>()

        subclasses.forEach { sub ->
            val subName = sub.simpleName.getShortName()
            val parent = sub.parent
            val isNested = parent is KSClassDeclaration && parent.simpleName.getShortName() == name

            if (isNested) {
                refList.add("$name.$subName()")
            } else {
                val subPackage = sub.packageName.asString()
                val importKey = "$subPackage.$subName"
                if (!importSet.contains(importKey)) {
                    sb.append("import ").append(importKey).append("\n")
                    importSet.add(importKey)
                }
                refList.add("$subName()")
            }
        }

        sb.append("fun ").append(name).append(".values(): List<").append(name).append("> = listOf(")
        sb.append(refList.joinToString(", "))
        sb.append(")\n")
        val file = codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            packageName,
            "${name}Values"
        )
        file.write(sb.toString().toByteArray())
        file.close()
    }
}
