plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        withJava()
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Ktor server
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
                
                // Serialization
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.coroutines.core)
                
                // Domain models
                implementation(project(":domain"))
                implementation(project(":data:core"))
                implementation(project(":data:sqldelight"))
            }
        }
    }
}