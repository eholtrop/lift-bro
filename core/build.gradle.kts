plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.sqldelight)

    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)

    id("io.gitlab.arturbosch.detekt") version("1.23.8")
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
    android {
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "core"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.kotlinx.datetime)
                implementation(libs.uuid)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlin.reflect)

                implementation(libs.colorpicker.compose)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ui.tooling.preview)
                implementation(libs.sqldelight.android.driver)
                implementation(libs.app.update)
                implementation(libs.app.update.ktx)
            }
        }

//        val nativeMain by getting {
//            dependencies {
//                implementation("app.cash.sqldelight:native-driver:2.0.0")
//            }
//        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.lift.bro"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
}