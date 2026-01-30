plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "tv.dpal.logging"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}

mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "logging",
        version = "0.1.0"
    )

    pom {
        name.set("Logging")
        description.set("Cross-platform logging utility for Kotlin Multiplatform")
        url.set("https://github.com/yourusername/logging")

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
            connection.set("scm:git:git://github.com/yourusername/logging.git")
            developerConnection.set("scm:git:ssh://github.com/yourusername/logging.git")
            url.set("https://github.com/yourusername/logging")
        }
    }
}
