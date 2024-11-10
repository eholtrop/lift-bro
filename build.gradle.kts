plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("8.2.2").apply(false)
    id("com.android.library").version("8.2.2").apply(false)
    id("org.jetbrains.compose").version("1.7.0").apply(false)
    id("com.google.gms.google-services") version "4.4.0" apply false
    kotlin("android").version("2.0.0").apply(false)
    kotlin("multiplatform").version("2.0.0").apply(false)
    kotlin("plugin.serialization") version "2.0.0"
    kotlin("plugin.compose").version("2.0.0").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
