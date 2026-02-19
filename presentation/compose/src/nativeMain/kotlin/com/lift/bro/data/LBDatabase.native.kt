package com.lift.bro.data

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory(
    encryptionKeyProvider: EncryptionKeyProvider,
) {
    actual fun provideDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver {
        return NativeSqliteDriver(
            schema = schema.synchronous(),
            databaseName = "liftbro.db",
        )
    }
}
