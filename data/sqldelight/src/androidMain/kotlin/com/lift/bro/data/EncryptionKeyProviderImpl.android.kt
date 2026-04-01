package com.lift.bro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.SecureRandom

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "liftbro_encrypted_prefs")

class EncryptionKeyProviderImpl(
    private val context: Context,
) : EncryptionKeyProvider {
    private companion object {
        private val KEY_DB_ENCRYPTION_KEY = stringPreferencesKey("db_encryption_key")
        private const val KEY_LENGTH_BYTES = 32
        private const val KEYSET_NAME = "liftbro_keyset"
        private const val PREFERENCE_FILE = "liftbro_keyset_prefs"
        private const val MASTER_KEY_URI = "android-keystore://liftbro_master_key"
        private const val ASSOCIATED_DATA = "liftbro"
    }

    private val keyMutex = Mutex()

    private val aead: Aead by lazy {
        AeadConfig.register()

        val keysetManager =
            AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()

        keysetManager.keysetHandle.getPrimitive(Aead::class.java)
    }

    override suspend fun getOrCreateKey(): ByteArray {
        keyMutex.withLock {
            val preferences = context.dataStore.data.first()
            val encryptedKeyBase64 = preferences[KEY_DB_ENCRYPTION_KEY]

            return if (encryptedKeyBase64 != null) {
                val encryptedKey = android.util.Base64.decode(encryptedKeyBase64, android.util.Base64.NO_WRAP)
                aead.decrypt(encryptedKey, ASSOCIATED_DATA.toByteArray())
            } else {
                val newKey = generateKey()
                val encryptedKey = aead.encrypt(newKey, ASSOCIATED_DATA.toByteArray())
                val encodedKey = android.util.Base64.encodeToString(encryptedKey, android.util.Base64.NO_WRAP)

                context.dataStore.edit { prefs ->
                    prefs[KEY_DB_ENCRYPTION_KEY] = encodedKey
                }
                newKey
            }
        }
    }

    private fun generateKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH_BYTES)
        SecureRandom().nextBytes(key)
        return key
    }
}
