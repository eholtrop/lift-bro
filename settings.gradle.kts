pluginManagement {
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

include(":libs:mvi:core")
include(":libs:mvi:compose")
include(":libs:logging")
include(":libs:ext:flow")
include(":libs:ext:ktx-datetime")
include(":libs:ext:compose")
