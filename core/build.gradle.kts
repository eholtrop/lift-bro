import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.lift.bro.versionCode
import com.lift.bro.versionName

plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.sqldelight)

    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.sentry)

    id("io.gitlab.arturbosch.detekt") version ("1.23.8")
}

sqldelight {
    databases {
        create("LiftBroDB") {
            packageName.set("com.lift.bro.db")
            generateAsync.set(true)
        }
    }
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
            // Compose Multiplatform
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.app.update)
            implementation(libs.app.update.ktx)
            implementation(libs.play.services.ads)
            implementation(compose.uiTooling)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

buildkonfig {

    defaultConfigs {
        packageName = "com.lift.bro.core.buildconfig"
        buildConfigField(FieldSpec.Type.STRING, "ADMOB_APP_ID", project.findProperty("LIFT_BRO_ADMOB_APP_ID") as? String ?: System.getenv("LIFT_BRO_ADMOB_APP_ID"))
        buildConfigField(FieldSpec.Type.STRING, "ADMOB_AD_UNIT_ID", project.findProperty("LIFT_BRO_AD_UNIT_ID") as? String ?: System.getenv("LIFT_BRO_AD_UNIT_ID"))
        buildConfigField(FieldSpec.Type.STRING, "SENTRY_DSN", project.findProperty("LIFT_BRO_SENTRY_DSN") as? String ?: System.getenv("LIFT_BRO_SENTRY_DSN"))
        buildConfigField(FieldSpec.Type.STRING, "REVENUE_CAT_API_KEY", project.findProperty("revenueCatApiKey") as? String ?: System.getenv("REVENUE_CAT_API_KEY"))
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", project.versionName())
    }
}

android {
    namespace = "com.lift.bro.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiiler.get()
    }

}

dependencies {
    implementation(libs.appcompat)
}
