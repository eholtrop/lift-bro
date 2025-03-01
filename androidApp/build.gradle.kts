import org.jetbrains.kotlin.load.kotlin.signatures
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)

    id("io.gitlab.arturbosch.detekt") version("1.23.8")
}

android {
    namespace = "com.lift.bro"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.lift.bro"
        minSdk = 24
        targetSdk = 34
        versionName = "${SimpleDateFormat("YYY-MM-dd").format(Date())}-alpha"


        versionCode = if (project.hasProperty("buildNumber")) {
            property("buildNumber").toString().toInt()
        } else {
            1
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
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
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.compose.activity)
    implementation(libs.navigation.compose)
}