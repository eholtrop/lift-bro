plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("LiftBroDB") {
            packageName.set("com.lift.bro.db")
            generateAsync.set(false)
        }
    }
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":libs:ext:ktx-datetime"))
            implementation(project(":domain"))
            implementation(project(":data:core"))
            implementation(project(":libs:logging"))
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.uuid)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.android.database.sqlcipher)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.lift.bro.data"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}


tasks.register("generateErDiagram") {
    group = "documentation"
    description = "Generates a Mermaid.js ER diagram of the SQLDelight database schema."
    notCompatibleWithConfigurationCache("Uses project references in doLast")

    doLast {
        // Each table: (tableName, columns) where columns is a list of maps
        val tableColumns = mutableListOf<Pair<String, MutableList<Map<String, Any>>>>()

        val sqDir = file("src/commonMain/sqldelight/com.lift.bro.db")
        val sqFiles = (sqDir.listFiles() ?: emptyArray()).filter { it.extension == "sq" }

        val createTableRegex = Regex(
            """CREATE TABLE (\w+)\s*\((.*?)\);""",
            RegexOption.DOT_MATCHES_ALL,
        )

        for (sqFile in sqFiles) {
            val content = sqFile.readText()

            for (match in createTableRegex.findAll(content)) {
                val tableName = match.groupValues[1]
                val columnsBlock = match.groupValues[2]
                val columns = mutableListOf<Map<String, Any>>()

                for (colDef in columnsBlock.split(",").map { it.trim() }.filter { it.isNotBlank() }) {
                    if (colDef.startsWith("PRIMARY KEY") || colDef.startsWith("FOREIGN KEY") ||
                        colDef.startsWith("CONSTRAINT") || colDef.startsWith("UNIQUE")
                    ) continue

                    val parts = colDef.split(Regex("\\s+"))
                    if (parts.size < 2) continue

                    val colName = parts[0]
                    val sqlType = parts[1]
                    val isPK = colDef.contains("PRIMARY KEY")
                    val refMatch = Regex("""REFERENCES\s+(\w+)\s*\(\w+\)""").find(colDef)
                    val foreignKey = refMatch?.groupValues?.get(1)

                    columns.add(
                        mapOf(
                            "name" to colName,
                            "sqlType" to sqlType,
                            "isPK" to isPK,
                            "foreignKey" to (foreignKey ?: ""),
                        ),
                    )
                }

                tableColumns.add(tableName to columns)
            }
        }

        val tableNames = tableColumns.map { it.first }.toSet()

        val resolvedTables = tableColumns.map { (tableName, columns) ->
            val resolved = columns.map { col ->
                val fk = col["foreignKey"] as String
                val name = col["name"] as String
                if (fk.isEmpty() && name != "id") {
                    val candidate = name.removeSuffix("Id").replaceFirstChar { it.uppercase() }
                    val inferred = if (candidate in tableNames) candidate else ""
                    col + ("foreignKey" to inferred)
                } else col
            }
            tableName to resolved
        }

        val mermaid = StringBuilder()
        mermaid.appendLine("erDiagram")

        for ((tableName, columns) in resolvedTables) {
            mermaid.appendLine("    $tableName {")
            for (col in columns) {
                val markers = mutableListOf<String>()
                if (col["isPK"] as Boolean) markers.add("PK")
                val fk = col["foreignKey"] as String
                if (fk.isNotEmpty()) markers.add("FK")
                val markerStr = if (markers.isNotEmpty()) " ${markers.joinToString(",")}" else ""
                mermaid.appendLine("        ${col["sqlType"]} ${col["name"]}$markerStr")
            }
            mermaid.appendLine("    }")
        }

        mermaid.appendLine()

        for ((tableName, columns) in resolvedTables) {
            for (col in columns) {
                val fk = col["foreignKey"] as String
                if (fk.isNotEmpty()) {
                    mermaid.appendLine("    $fk ||--o{ $tableName : \"\"")
                }
            }
        }

        val mermaidContent = mermaid.toString()

        val docsDir = rootProject.file("docs")
        docsDir.mkdirs()
        val standaloneFile = File(docsDir, "database-er-diagram.md")
        standaloneFile.writeText(
            buildString {
                appendLine("# Database ER Diagram")
                appendLine()
                appendLine("Generated using `./gradlew :data:sqldelight:generateErDiagram`")
                appendLine()
                appendLine("```mermaid")
                append(mermaidContent)
                appendLine("```")
                appendLine()
            },
        )

        val readmeFile = rootProject.file("README.md")
        if (readmeFile.exists()) {
            val readmeContent = readmeFile.readText()
            val startMarker = "<!-- er-diagram-start -->"
            val endMarker = "<!-- er-diagram-end -->"
            val startIdx = readmeContent.indexOf(startMarker)
            val endIdx = readmeContent.indexOf(endMarker)

            if (startIdx != -1 && endIdx != -1) {
                val newContent = readmeContent.substring(0, startIdx + startMarker.length) +
                    "\n```mermaid\n$mermaidContent```\n" +
                    readmeContent.substring(endIdx)
                readmeFile.writeText(newContent)
            }
        }

        println("ER diagram generated:")
        println("  - docs/database-er-diagram.md")
        println("  - README.md (Database Schema section)")
    }
}

tasks.named("generateSqlDelightInterface") {
    finalizedBy("generateErDiagram")
}
