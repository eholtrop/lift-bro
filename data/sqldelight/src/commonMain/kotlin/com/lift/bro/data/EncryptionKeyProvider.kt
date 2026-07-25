package com.lift.bro.data

interface EncryptionKeyProvider {
    suspend fun getOrCreateKey(): ByteArray
}
