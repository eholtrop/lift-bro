package tv.dpal.logging

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}
