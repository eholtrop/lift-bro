package com.lift.bro

import org.gradle.api.Project
import java.text.SimpleDateFormat
import java.util.Date

fun Project.versionCode(): Int {
    return if (project.hasProperty("buildNumber")) {
        property("buildNumber").toString().toInt() + 156
    } else {
        1
    }
}

fun Project.versionName(): String {
    return SimpleDateFormat("yyyy.MM.dd").format(Date())
}