package com.lift.bro.data

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory

actual class DriverFactory actual constructor(
    @Suppress("UNUSED_PARAMETER") context: Any,
) {
    private val androidContext: Context = context as Context
    private val encryptionKeyProvider = EncryptionKeyProviderImpl(androidContext)

    actual fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ): SqlDriver {
        val encryptionKey = runBlocking { encryptionKeyProvider.getOrCreateKey() }
        val passphrase = String(encryptionKey, Charsets.UTF_8)

        val factory = SupportFactory(passphrase.toByteArray(Charsets.UTF_8))

        return AndroidSqliteDriver(
            schema = schema.synchronous(),
            context = androidContext,
            name = "test.db",
//            factory = factory,
        )
    }
}
