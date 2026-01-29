plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.publish)
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
            implementation(compose.runtimeSaveable)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
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
    namespace = "tv.dpal.swipenavhost"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() }
}

mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "swipenavhost",
        version = "0.1.0"
    )

    pom {
        name.set("SwipeNavHost")
        description.set("Swipeable navigation component for Compose Multiplatform with MVI integration")
        url.set("https://github.com/yourusername/swipenavhost")

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
            connection.set("scm:git:git://github.com/yourusername/swipenavhost.git")
            developerConnection.set("scm:git:ssh://github.com/yourusername/swipenavhost.git")
            url.set("https://github.com/yourusername/swipenavhost")
        }
    }
}
