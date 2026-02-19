package com.lift.bro.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

class EncryptionKeyProviderImpl(
    private val context: Context,
) : EncryptionKeyProvider {

    private companion object {
        private const val PREFS_NAME = "liftbro_encrypted_prefs"
        private const val KEY_DB_ENCRYPTION_KEY = "db_encryption_key"
        private const val KEY_LENGTH_BYTES = 32
    }

    private val masterKey: MasterKey by lazy {
        val spec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        MasterKey.Builder(context)
            .setKeyGenParameterSpec(spec)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun getOrCreateKey(): ByteArray {
        val existingKey = encryptedPrefs.getString(KEY_DB_ENCRYPTION_KEY, null)
        return if (existingKey != null) {
            android.util.Base64.decode(existingKey, android.util.Base64.NO_WRAP)
        } else {
            val newKey = generateKey()
            val encodedKey = android.util.Base64.encodeToString(newKey, android.util.Base64.NO_WRAP)
            encryptedPrefs.edit().putString(KEY_DB_ENCRYPTION_KEY, encodedKey).apply()
            newKey
        }
    }

    private fun generateKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH_BYTES)
        SecureRandom().nextBytes(key)
        return key
    }
}
