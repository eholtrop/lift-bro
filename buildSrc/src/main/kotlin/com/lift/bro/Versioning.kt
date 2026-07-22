package com.lift.bro

import org.gradle.api.Project

fun Project.versionCode(): Int {
    return if (project.hasProperty("buildNumber")) {
        property("buildNumber").toString().toInt()
    } else {
        1
    }
}

fun Project.versionName(): String {
    return if (project.hasProperty("versionName")) {
        property("versionName").toString()
    } else {
        "local-build"
    }
}
