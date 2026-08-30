package com.lift.bro

internal fun decodeBase64(encoded: String): ByteArray {
    val lookup = IntArray(128) { -1 }
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    for ((i, c) in chars.withIndex()) {
        lookup[c.code] = i
    }
    lookup['='.code] = 0

    val input = encoded.filter { !it.isWhitespace() }
    val outputLength = input.length / 4 * 3 - (input.takeLast(2).count { it == '=' })
    val output = ByteArray(outputLength)

    var outputIndex = 0
    var i = 0
    while (i < input.length) {
        val a = lookup[input[i].code]
        val b = lookup[input[i + 1].code]
        val c = if (input[i + 2] == '=') 0 else lookup[input[i + 2].code]
        val d = if (input[i + 3] == '=') 0 else lookup[input[i + 3].code]

        val triple = (a shl 18) or (b shl 12) or (c shl 6) or d

        if (outputIndex < outputLength) output[outputIndex++] = (triple shr 16).toByte()
        if (outputIndex < outputLength) output[outputIndex++] = (triple shr 8).toByte()
        if (outputIndex < outputLength) output[outputIndex++] = triple.toByte()

        i += 4
    }
    return output
}
