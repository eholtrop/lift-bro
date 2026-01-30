plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "tv.dpal.flowvi"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}

mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "flowvi-core",
        version = "0.1.0"
    )

    pom {
        name.set("FlowVi Core")
        description.set("MVI (Model-View-Intent) framework for Kotlin Multiplatform")
        url.set("https://github.com/dpaltv/flowvi")

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
            connection.set("scm:git:git://github.com/dpaltv/flowvi.git")
            developerConnection.set("scm:git:ssh://github.com/dpaltv/flowvi.git")
            url.set("https://github.com/dpaltv/flowvi")
        }
    }
}
