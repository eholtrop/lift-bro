package com.lift.bro.data

expect class DriverFactory(
    encryptionKeyProvider: EncryptionKeyProvider,
) {
    fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    ): SqlDriver
}
