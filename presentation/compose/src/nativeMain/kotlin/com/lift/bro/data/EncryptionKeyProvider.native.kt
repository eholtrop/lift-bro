package com.lift.bro.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults

@OptIn(ExperimentalForeignApi::class)
class EncryptionKeyProviderImpl : EncryptionKeyProvider {

    private companion object {
        private const val SERVICE_NAME = "com.liftbro.database"
        private const val KEY_LENGTH_BYTES = 32
    }

    private val userDefaults: NSUserDefaults by lazy {
        NSUserDefaults.standardUserDefaults
    }

    suspend fun getOrCreateKey(): ByteArray {
        val existingKey = getKeyFromStorage()
        return existingKey ?: generateAndStoreKey()
    }

    private fun getKeyFromStorage(): ByteArray? {
        val data = userDefaults.dataForKey(SERVICE_NAME)
        return data?.let {
            val nsData = NSData.dataWithData(it)
            nsData.toByteArray()
        }
    }

    @OptIn(kotlin.ExperimentalNativeApi::class)
    private fun generateAndStoreKey(): ByteArray {
        val key = ByteArray(KEY_LENGTH_BYTES)
        kotlinx.native.internal.SystemRandom.nextBytes(key)
        
        val nsData = NSData(bytes = key.refTo(0), length = KEY_LENGTH_BYTES)
        userDefaults.setObject(nsData, forKey = SERVICE_NAME)
        userDefaults.synchronize()

        return key
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytesRef = this.bytes
    val len = this.length.toInt()
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
