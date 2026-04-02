package com.lift.bro.data.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class UserPreferencesDataSource {

    val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun putString(key: String, value: String?) {
        userDefaults.setObject(value, key)
    }

    actual fun getString(key: String, default: String?): String? {
        return userDefaults.stringForKey(key)
    }

    actual fun putInt(key: String, value: Int) {
        userDefaults.setInteger(value.toLong(), key)
    }

    actual fun getInt(key: String, default: Int): Int {
        return userDefaults.integerForKey(key).toInt()
    }

    actual fun putBool(key: String, value: Boolean) {
        userDefaults.setBool(value, key)
    }

    actual fun getBool(key: String, default: Boolean): Boolean {
        if (userDefaults.objectForKey(key) == null) {
            userDefaults.setBool(default, key)
        }
        return userDefaults.boolForKey(key)
    }

    actual inline fun <reified T> putSerializable(key: String, value: T?) {
        userDefaults.setObject(Json.encodeToString(value), key)
    }

    actual inline fun <reified T> getSerializable(key: String, default: T?): T? {
        return userDefaults.stringForKey(key)?.let { Json.decodeFromString<T?>(it) } ?: default
    }
}
