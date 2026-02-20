package com.lift.bro.data

import android.content.Context
import android.util.Log
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.lift.bro.db.LiftBroDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import java.io.File

class DatabaseMigrationManager(
    private val context: Context,
    private val encryptionKeyProvider: EncryptionKeyProvider,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun migrateIfNeeded(): MigrationResult = withContext(Dispatchers.IO) {
        val migrationRequired = context.databaseList().let {
            !it.contains(ENCRYPTED_DB_NAME) || it.contains(UNENCRYPTED_DB_NAME)
        }

        if (migrationRequired) {
            Log.d(TAG, "Performing Migration")
            performMigration(
                unencryptedDb = context.getDatabasePath(UNENCRYPTED_DB_NAME),
                encryptedDb = context.getDatabasePath(ENCRYPTED_DB_NAME),
            )
        } else {
            Log.d(TAG, "No Migration Required")
            MigrationResult.NoMigrationNeeded
        }
    }

    private suspend fun performMigration(unencryptedDb: File, encryptedDb: File): MigrationResult = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_MIGRATION_STATE, MIGRATION_IN_PROGRESS).apply()

        var targetDriver: SqlDriver? = null

        try {
            val passphrase = runBlocking { encryptionKeyProvider.getOrCreateKey() }
            val passphraseString = String(passphrase, Charsets.UTF_8)
            val passphraseBytes = passphraseString.toByteArray(Charsets.UTF_8)

            val unencryptedSize = unencryptedDb.length()
            Log.d(TAG, "Unencrypted DB size: $unencryptedSize bytes")

            val targetFactory = SupportFactory(passphraseBytes)
            targetDriver = AndroidSqliteDriver(
                schema = LiftBroDB.Schema.synchronous(),
                context = context,
                name = ENCRYPTED_DB_NAME,
                factory = targetFactory,
            )

            copyAllTables(targetDriver, unencryptedDb.absolutePath)

            targetDriver.close()
            targetDriver = null

            if (!encryptedDb.exists()) {
                Log.e(TAG, "Encrypted DB was not created!")
                prefs.edit().putString(KEY_MIGRATION_STATE, null).apply()
                return@withContext MigrationResult.Failed("Encrypted DB was not created")
            }

            unencryptedDb.delete()
            Log.i(TAG, "Migration completed successfully!")

            prefs.edit()
                .putString(KEY_MIGRATION_STATE, MIGRATION_COMPLETE)
                .apply()

            MigrationResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed", e)
            encryptedDb.delete()
            prefs.edit().putString(KEY_MIGRATION_STATE, null).apply()
            MigrationResult.Failed(e.message ?: "Unknown error")
        } finally {
            try {
                targetDriver?.close()
            } catch (e: Exception) { /* ignore */
            }
        }
    }

    private fun copyAllTables(targetDriver: SqlDriver, sourceDbPath: String) {
        Log.d(TAG, "Copying all tables using ATTACH")

        try {
            // For SQLCipher, to attach an unencrypted database, use empty KEY
            val attachSql = "ATTACH DATABASE '$sourceDbPath' AS source KEY ''"
            targetDriver.execute(null, attachSql, 0, null)
            Log.d(TAG, "ATTACH succeeded")
        } catch (e: Exception) {
            Log.e(TAG, "ATTACH failed: ${e.message}")
            throw e
        }

        val tables = listOf("Lift", "Variation", "LiftingSet", "LiftingLog", "Workout", "Goal", "Exercise", "ExerciseVariation")

        for (table in tables) {
            Log.d(TAG, "Copying table: $table")
            try {
                val insertSql = "INSERT OR REPLACE INTO $table SELECT * FROM source.$table"
                targetDriver.execute(null, insertSql, 0, null)
                Log.d(TAG, "Copied table: $table")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to copy table $table: ${e.message}")
                throw e
            }
        }

        try {
            targetDriver.execute(null, "DETACH DATABASE source", 0, null)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to detach source DB: ${e.message}")
        }
    }

    fun cleanupIfComplete() {
        if (prefs.getString(KEY_MIGRATION_STATE, null) == MIGRATION_COMPLETE) {
            if (context.databaseList().contains(UNENCRYPTED_DB_NAME)) {
                Log.i(TAG, "Cleaning up old unencrypted DB after successful migration")
                context.getDatabasePath(UNENCRYPTED_DB_NAME).delete()
            }
        }
    }

    sealed class MigrationResult {
        data object Success: MigrationResult()
        data object NoMigrationNeeded: MigrationResult()
        data class Failed(val reason: String): MigrationResult()
    }

    companion object {
        private const val TAG = "DatabaseMigration"
        private const val PREFS_NAME = "liftbro_migration_prefs"
        private const val KEY_MIGRATION_STATE = "migration_state"
        private const val MIGRATION_IN_PROGRESS = "in_progress"
        private const val MIGRATION_COMPLETE = "complete"
        private const val UNENCRYPTED_DB_NAME = "test.db"
        private const val ENCRYPTED_DB_NAME = "liftbro.db"
    }
}
