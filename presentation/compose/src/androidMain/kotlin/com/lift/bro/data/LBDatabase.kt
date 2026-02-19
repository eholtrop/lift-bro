package com.lift.bro.data

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.runBlocking

actual class DriverFactory(
    encryptionKeyProvider: EncryptionKeyProvider,
) {

    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    actual fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ): SqlDriver {
        val ctx = context ?: throw IllegalStateException("Context not set")
        
        return AndroidSqliteDriver(
            schema = schema.synchronous(),
            context = ctx,
            name = "liftbro.db",
        )
    }
}
