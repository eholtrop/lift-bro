import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)

    id("io.gitlab.arturbosch.detekt") version ("1.23.8")
}

android {
    namespace = "com.lift.bro"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.lift.bro"
        minSdk = 24
        targetSdk = 35
        versionName = "${SimpleDateFormat("YYY-MM-dd").format(Date())}-alpha"

        versionCode = if (project.hasProperty("buildNumber")) {
            property("buildNumber").toString().toInt()
        } else {
            1
        }
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
            buildConfigField("String", "AD_UNIT_ID", project.findProperty("LIFT_BRO_AD_UNIT_ID") as? String ?: "\"${System.getenv("LIFT_BRO_AD_UNIT_ID")}\"")
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
    implementation(project(":core"))
    implementation(libs.compose.activity)
    implementation(libs.kotlinx.serialization)
    implementation(libs.play.services.ads)
}