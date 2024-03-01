plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("8.1.1").apply(false)
    id("com.android.library").version("8.1.1").apply(false)
    id("org.jetbrains.compose").version("1.5.2").apply(false)
    id("com.google.gms.google-services") version "4.4.0" apply false
    kotlin("android").version("1.8.21").apply(false)
    kotlin("multiplatform").version("1.8.21").apply(false)
    kotlin("plugin.serialization") version "1.8.21"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
