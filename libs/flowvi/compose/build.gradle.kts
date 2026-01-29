plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.publish)
    id("org.jetbrains.dokka") version "2.0.0"
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // TODO: Replace with published dependencies when available
            // api("tv.dpal:flowvi-core:<version>")
            // implementation("tv.dpal:logging:<version>")
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(compose.runtime)
            implementation("org.jetbrains.compose.runtime:runtime-saveable:1.8.0")
        }
        androidMain.dependencies {
            // no-op
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "tv.dpal.flowvi.compose"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() }
}

// Generate HTML docs into libs/mvi/docs
tasks.register<org.jetbrains.dokka.gradle.DokkaTask>("mviDokkaHtml") {
    outputDirectory.set(file("${project.projectDir}/docs"))
}

mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "flowvi-compose",
        version = "0.1.0"
    )

    pom {
        name.set("FlowVi Compose")
        description.set("Compose Multiplatform integration for FlowVi MVI framework")
        url.set("https://github.com/yourusername/flowvi")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("yourusername")
                name.set("Your Name")
                email.set("your.email@example.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/yourusername/flowvi.git")
            developerConnection.set("scm:git:ssh://github.com/yourusername/flowvi.git")
            url.set("https://github.com/yourusername/flowvi")
        }
    }
}
