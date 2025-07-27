package com.lift.bro.data.datasource

expect class UserPreferencesDataSource {

    fun putString(key: String, value: String?)

    fun getString(key: String, default: String? = null): String?

    fun putInt(key: String, value: Int)

    fun getInt(key: String, default: Int): Int

    fun putBool(key: String, value: Boolean)

    fun getBool(key: String, default: Boolean): Boolean

    inline fun <reified T> getSerializable(key: String, default: T?): T?

    inline fun <reified T> putSerializable(key: String, value: T?)
}