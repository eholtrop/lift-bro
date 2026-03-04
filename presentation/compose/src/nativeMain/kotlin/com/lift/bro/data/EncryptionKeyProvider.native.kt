package com.lift.bro.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
class EncryptionKeyProviderImpl : EncryptionKeyProvider {

    private companion object {
        private const val SERVICE_NAME = "com.liftbro.database"
        private const val KEY_LENGTH_BYTES = 32
    }

    private val userDefaults: NSUserDefaults by lazy {
        NSUserDefaults.standardUserDefaults
    }

    override suspend fun getOrCreateKey(): ByteArray {
        val existingKey = getKeyFromStorage()
        return existingKey ?: generateAndStoreKey()
    }

    private fun getKeyFromStorage(): ByteArray? {
        val data = userDefaults.dataForKey(SERVICE_NAME)
        return data?.let {
            nsDataToByteArray(it)
        }
    }

    private fun generateAndStoreKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH_BYTES)
        Random.nextBytes(key)

        val nsData = NSData(bytes = key.refTo(0), length = KEY_LENGTH_BYTES)
        userDefaults.setObject(nsData, forKey = SERVICE_NAME)
        userDefaults.synchronize()

        return key
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun nsDataToByteArray(nsData: NSData): ByteArray {
        val bytesRef = nsData.bytes
        val len = nsData.length.toInt()
        return if (len > 0) {
            val result = ByteArray(len)
            for (i in 0 until len) {
                result[i] = bytesRef[i].toByte()
            }
            result
        } else {
            ByteArray(0)
        }
    }
}

