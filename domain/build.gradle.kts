import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.lift.bro.versionName

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)

    id("io.gitlab.arturbosch.detekt") version ("1.23.8")
}

buildkonfig {
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        packageName = "com.lift.bro.core.buildconfig"
        buildConfigField(FieldSpec.Type.STRING, "ADMOB_APP_ID", project.findProperty("LIFT_BRO_ADMOB_APP_ID") as? String ?: System.getenv("LIFT_BRO_ADMOB_APP_ID"))
        buildConfigField(FieldSpec.Type.STRING, "ADMOB_AD_UNIT_ID", project.findProperty("LIFT_BRO_AD_UNIT_ID") as? String ?: System.getenv("LIFT_BRO_AD_UNIT_ID"))
        buildConfigField(FieldSpec.Type.STRING, "SENTRY_DSN", project.findProperty("LIFT_BRO_SENTRY_DSN") as? String ?: System.getenv("LIFT_BRO_SENTRY_DSN"))
        buildConfigField(FieldSpec.Type.STRING, "REVENUE_CAT_API_KEY_AND", project.findProperty("revenueCatApiKeyAndroid") as? String ?: System.getenv("REVENUE_CAT_API_KEY"))
        buildConfigField(FieldSpec.Type.STRING, "REVENUE_CAT_API_KEY_IOS", project.findProperty("revenueCatApiKeyiOS") as? String ?: System.getenv("REVENUE_CAT_API_KEY"))
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", project.versionName())
    }
}

kotlin {
    androidTarget()
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.uuid)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.lift.bro.domain"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}
