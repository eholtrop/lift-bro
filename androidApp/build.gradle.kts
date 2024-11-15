import org.jetbrains.kotlin.load.kotlin.signatures

plugins {
    kotlin("android")
    kotlin("plugin.compose")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.lift.bro"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.lift.bro"
        minSdk = 24
        targetSdk = 34
        versionName = "0.0.1-alpha"


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
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // unsure about this
    implementation("com.google.firebase:firebase-analytics")
}