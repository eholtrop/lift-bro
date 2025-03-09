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

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.navigation.compose)


                implementation(libs.kotlinx.datetime)
                implementation("com.benasher44:uuid:0.8.1")
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlinx.serialization)

                implementation("com.github.skydoves:colorpicker-compose:1.1.2")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.5.2")
                implementation(libs.sqldelight.android.driver)
                implementation(libs.app.update)
                implementation(libs.app.update.ktx)
                implementation(libs.ui.tooling.preview.android)
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
kotlin {
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
    }
}