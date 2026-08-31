plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

val screenshotBackupOutput =
    rootProject.layout.buildDirectory.dir("generated/test-data/screenshot_test_backup.json").get().asFile

kotlin {
    androidTarget()
    jvm()
    iosX64()
    iosArm64("ios")
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.kotlinx.datetime)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.register<JavaExec>("generateTestData") {
    group = "tools"
    description = "Generates build/generated/test-data/screenshot_test_backup.json via TestDataGenerator"
    val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")
    classpath(jvmMainCompilation.runtimeDependencyFiles, jvmMainCompilation.output.allOutputs)
    mainClass.set("com.lift.bro.backup.tools.TestDataGeneratorKt")
    val outputPath = providers.gradleProperty("backupOutput").orNull ?: screenshotBackupOutput.absolutePath
    systemProperty("backupOutputPath", outputPath)
}

android {
    namespace = "com.lift.bro.backup"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}