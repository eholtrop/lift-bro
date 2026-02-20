package com.lift.bro.data

import android.content.Context
import android.util.Log
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
    private val migrationManager by lazy { DatabaseMigrationManager(androidContext, encryptionKeyProvider) }

    actual fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ): SqlDriver {
        runBlocking {
            migrationManager.migrateIfNeeded()
        }

        migrationManager.cleanupIfComplete()

        val encryptedDb = androidContext.getDatabasePath(ENCRYPTED_DB_NAME)
        val passphrase = runBlocking { encryptionKeyProvider.getOrCreateKey() }
        val passphraseBytes = passphrase.toString(Charsets.UTF_8).toByteArray(Charsets.UTF_8)

        return if (encryptedDb.exists()) {
            try {
                AndroidSqliteDriver(
                    schema = schema.synchronous(),
                    context = androidContext,
                    name = ENCRYPTED_DB_NAME,
                    factory = SupportFactory(passphraseBytes),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open encrypted database", e)
                throw e
            }
        } else {
            AndroidSqliteDriver(
                schema = schema.synchronous(),
                context = androidContext,
                name = ENCRYPTED_DB_NAME,
                factory = SupportFactory(passphraseBytes),
            )
        }
    }

    companion object {
        private const val TAG = "DriverFactory"
        private const val ENCRYPTED_DB_NAME = "liftbro.db"
    }
}
