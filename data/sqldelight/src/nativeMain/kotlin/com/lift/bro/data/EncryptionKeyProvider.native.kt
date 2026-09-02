package com.lift.bro.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.posix.arc4random_buf
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, UnsafeNumber::class)
class EncryptionKeyProviderImpl: EncryptionKeyProvider {

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
        key.usePinned { pinned ->
            arc4random_buf(pinned.addressOf(0), KEY_LENGTH_BYTES.convert())
        }

        val nsData = key.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = key.size.convert())
        }
        userDefaults.setObject(nsData, forKey = SERVICE_NAME)
        userDefaults.synchronize()

        return key
    }

    private fun nsDataToByteArray(nsData: NSData): ByteArray = ByteArray(nsData.length.toInt()).apply {
        memcpy(this.refTo(0), nsData.bytes, nsData.length)
    }
}