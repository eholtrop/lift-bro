import com.android.utils.jvmArchitecture
import com.lift.bro.versionCode
import com.lift.bro.versionName
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
}

android {
    namespace = "com.lift.bro"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.lift.bro"
        minSdk = 24
        targetSdk = 36
        versionName = project.versionName()

        versionCode = project.versionCode()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        register("release") {
            storeFile = file("release.jks")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    implementation(project(":presentation:compose"))
    implementation(project(":presentation:server"))
    implementation(project(":domain"))
    implementation(libs.compose.activity)
    implementation(libs.kotlinx.serialization)
    implementation(libs.billing.ktx)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
