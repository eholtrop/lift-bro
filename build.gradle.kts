plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.google.services) apply false

    alias(libs.plugins.buildkonfig) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
