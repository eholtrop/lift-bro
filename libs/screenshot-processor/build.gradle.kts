import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)
//    implementation(libs.kotlin.stdlib)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
