package com.lift.bro

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform