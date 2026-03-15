plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
    }
}

android {
    namespace = "com.lift.bro.annotation"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}
