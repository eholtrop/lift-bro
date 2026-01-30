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
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(compose.runtime)
            implementation(compose.runtimeSaveable)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "tv.dpal.navi"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() }
}

mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "navi",
        version = "0.1.0"
    )

    pom {
        name.set("Navi")
        description.set("Navigation Controller and SwipeableNavHost for Compose Multiplatform")
        url.set("https://github.com/dpaltv/navi")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("eholtrop")
                name.set("Evan Holtrop")
                email.set("admin@dangeroustoplayalone.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/dpaltv/navi.git")
            developerConnection.set("scm:git:ssh://github.com/dpaltv/navi.git")
            url.set("https://github.com/dpaltv/navi")
        }
    }
}
