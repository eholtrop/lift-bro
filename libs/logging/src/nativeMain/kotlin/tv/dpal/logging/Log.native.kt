package tv.dpal.logging

actual fun Log.d(tag: String?, message: String) {
    println("[$tag] $message")
}
