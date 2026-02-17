package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.ATProtoCredentials

interface FeedAuthDataSource {
    suspend fun createSession(
        handle: String,
        password: String,
    ): Result<ATProtoCredentials>

    suspend fun createSessionOAuth(pdsHost: String): Result<ATProtoCredentials>

    suspend fun refreshSession(credentials: ATProtoCredentials): Result<ATProtoCredentials>
}
