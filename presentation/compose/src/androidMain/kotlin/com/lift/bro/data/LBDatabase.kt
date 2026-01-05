package com.lift.bro.data

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(
    private val context: Context,
) {

    actual fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ): SqlDriver {
        return AndroidSqliteDriver(schema.synchronous(), context, "test.db")
    }
}
