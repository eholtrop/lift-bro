import com.lift.bro.versionCode
import com.lift.bro.versionName

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false

    id("io.gitlab.arturbosch.detekt") version ("1.23.8")
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiiler.get()
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
        defaultConfig {
            resValue("string", "admob_app_id", project.findProperty("LIFT_BRO_ADMOB_APP_ID") as? String ?: System.getenv("LIFT_BRO_ADMOB_APP_ID"))
        }


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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":app-compose"))
    implementation(libs.compose.activity)
    implementation(libs.kotlinx.serialization)
    implementation(libs.play.services.ads)
    implementation(libs.billing.ktx)
}