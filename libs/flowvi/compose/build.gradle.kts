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
            api(project(":libs:flowvi:core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(compose.runtime)
            implementation(compose.runtimeSaveable)
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

mavenPublishing {
    coordinates(
        groupId = "tv.dpal",
        artifactId = "flowvi-compose",
        version = "0.1.0"
    )

    pom {
        name.set("FlowVi Compose")
        description.set("Compose Multiplatform integration for FlowVi MVI framework")
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
