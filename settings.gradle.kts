pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useVersion("2.1.21-2.0.1")
            }
        }
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "Lift_Bro"
include(":app-android")
include(":data")
include(":data:client")
include(":domain")
include(":presentation:compose")
include(":presentation:server")

include(":data:sqldelight")
include(":data:core")
include(":data:atproto")

//include(":libs:flowvi:core")
//include(":libs:flowvi:compose")
include(":libs:logging")
include(":libs:ext:flow")
include(":libs:ext:ktx-datetime")
include(":libs:ext:compose")
include(":libs:navi")
include(":libs:screenshot-processor")
