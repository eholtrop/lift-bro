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
            api(project(":domain"))
            implementation(project(":data:sqldelight"))
            implementation(project(":data:client"))
            api(project(":data:core"))

            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.android.database.sqlcipher)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

android {
    namespace = "com.lift.bro.di"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}

dependencies {
    implementation(libs.appcompat)
}
