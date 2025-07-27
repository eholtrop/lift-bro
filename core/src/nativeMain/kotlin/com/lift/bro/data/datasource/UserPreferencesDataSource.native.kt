package com.lift.bro.data.datasource

actual class UserPreferencesDataSource {

    actual fun putString(key: String, value: String?) {
    }

    actual fun getString(key: String, default: String?): String? {
        TODO("Not yet implemented")
    }

    actual fun putInt(key: String, value: Int) {
    }

    actual fun getInt(key: String, default: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun putBool(key: String, value: Boolean?) {
    }

    actual fun getBool(key: String, default: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    actual inline fun <reified T> putSerializable(key: String, value: T?) {
    }

    actual inline fun <reified T> getSerializable(key: String, default: T?): T? {
        TODO("Not yet implemented")
    }

}