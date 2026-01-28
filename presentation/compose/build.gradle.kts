import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.lift.bro.versionCode
import com.lift.bro.versionName

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sentry)
}

compose.resources {
    packageOfResClass = "lift_bro.core.generated.resources"
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {

    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "core"
            isStatic = true
            binaryOption("bundleId", "com.lift.bro.core")
            binaryOption("bundleVersion", project.versionCode().toString())
            binaryOption("bundleShortVersionString", project.versionName())
        }
    }

    sourceSets {

        // Required for Revenuecat
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }


        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":data:sqldelight"))
            implementation(project(":data:client"))
            implementation(project(":data:core"))
            implementation(project(":libs:mvi:core"))
            implementation(project(":libs:mvi:compose"))
            implementation(project(":libs:ext:flow"))
            implementation(project(":libs:logging"))
            implementation(project(":libs:ext:ktx-datetime"))
            implementation(project(":libs:ext:compose"))
            api(project(":libs:swipenavhost"))

            // Compose Multiplatform
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
            implementation("org.jetbrains.compose.ui:ui-backhandler:1.8.0")

            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlin.reflect)

            implementation(libs.colorpicker.compose)
            implementation(libs.kotlinx.coroutines.core)

            // File picker/sharing
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)

            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.datetime)
            implementation(libs.revenuecat.ui)


//            implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
            implementation("dev.gitlive:firebase-crashlytics:2.1.0")
            implementation("dev.gitlive:firebase-analytics:2.1.0")
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.app.update)
            implementation(libs.app.update.ktx)
            implementation(compose.uiTooling)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}


android {
    namespace = "com.lift.bro.core"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

}

dependencies {
    implementation(libs.appcompat)
}
