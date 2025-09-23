package com.lift.bro.data.datasource

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class UserPreferencesDataSource(
    private val context: Context
) {

    val sharedPreferences by lazy {
        context.getSharedPreferences("lift.bro.prefs", MODE_PRIVATE)
    }

    actual fun putString(key: String, value: String?) {
        sharedPreferences.edit { putString(key, value) }
    }

    actual fun getString(key: String, default: String?): String? {
        return sharedPreferences.getString(key, default)
    }

    actual fun putInt(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }

    actual fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    actual fun putBool(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    actual fun getBool(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    actual inline fun <reified T> putSerializable(key: String, value: T?) {
        sharedPreferences.edit { putString(key, Json.encodeToString(value)) }
    }

    actual inline fun <reified T> getSerializable(key: String, default: T?): T? {
        return sharedPreferences.getString(key, null)?.let {
            Json.decodeFromString(it)
        } ?: default
    }

}