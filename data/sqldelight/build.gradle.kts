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
            generateAsync.set(true)
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
            implementation(libs.androidx.security.crypto)
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
