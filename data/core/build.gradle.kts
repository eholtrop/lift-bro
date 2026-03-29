plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.revenuecat.core)
            implementation(libs.revenuecat.datetime)
            implementation(libs.uuid)
        }
        androidMain.dependencies {
            implementation(libs.filekit.dialogs)
        }
        iosMain.dependencies {
            implementation(libs.filekit.dialogs)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.lift.bro.data.core"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}

dependencies {
    implementation(libs.appcompat)
}
