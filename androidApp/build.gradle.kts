plugins {
    kotlin("android")
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
        versionCode = 2
        versionName = "1.0"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // unsure about this
    implementation("com.google.firebase:firebase-analytics")
}