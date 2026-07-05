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
    return System.getenv("LIFT_BRO_COMMIT_DATE")
        ?: SimpleDateFormat("yyyy.MM.dd").format(Date())
}