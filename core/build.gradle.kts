plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("app.cash.sqldelight") version "2.0.0"
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
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
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
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

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)


                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                implementation("com.benasher44:uuid:0.8.1")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")


                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
                implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.5.2")
                implementation("app.cash.sqldelight:android-driver:2.0.0")

                implementation("com.google.android.play:app-update:2.1.0")
                implementation("com.google.android.play:app-update-ktx:2.1.0")
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
