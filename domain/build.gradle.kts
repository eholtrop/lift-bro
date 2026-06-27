import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.lift.bro.versionName

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

buildkonfig {
    exposeObjectWithName = "BuildKonfig"
    packageName = "com.lift.bro.core.buildconfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "SENTRY_DSN", project.findProperty("LIFT_BRO_SENTRY_DSN") as? String ?: System.getenv("LIFT_BRO_SENTRY_DSN") ?: "")
        buildConfigField(FieldSpec.Type.STRING, "POSTHOG_API_KEY", project.findProperty("postHogApiKey") as? String ?: System.getenv("POSTHOG_API_KEY") ?: "")
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", project.versionName())
        buildConfigField(FieldSpec.Type.STRING, "REVENUE_CAT_API_KEY", "")
    }

    targetConfigs {
        create("android") {
            buildConfigField(FieldSpec.Type.STRING, "REVENUE_CAT_API_KEY", project.findProperty("revenueCatApiKeyAndroid") as? String ?: System.getenv("REVENUE_CAT_API_KEY") ?: "")

        }
        create("ios") {
            buildConfigField(FieldSpec.Type.STRING, "REVENUE_CAT_API_KEY", project.findProperty("revenueCatApiKeyiOS") as? String ?: System.getenv("REVENUE_CAT_API_KEY") ?: "")
        }
    }
}

kotlin {
    androidTarget()
    jvm()
    iosX64()
    iosArm64("ios")
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
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.lift.bro.domain"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}
