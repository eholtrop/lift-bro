package com.lift.bro.domain

fun <T1, T2, R> ifLet(v1: T1?, v2: T2?, completion: (T1, T2) -> R?): R? {
    return if (v1 == null || v2 == null) {
        null
    } else {
        completion(v1, v2)
    }
}
