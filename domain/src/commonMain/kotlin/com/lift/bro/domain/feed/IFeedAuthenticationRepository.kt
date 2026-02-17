package com.lift.bro.domain.feed

import com.lift.bro.domain.models.ATProtoCredentials
import kotlinx.coroutines.flow.Flow

interface IFeedAuthenticationRepository {
    suspend fun authenticate(
        handle: String,
        password: String,
    ): Result<ATProtoCredentials>

    suspend fun startOAuth(pdsHost: String): Result<ATProtoCredentials>

    suspend fun refreshSession(credentials: ATProtoCredentials): Result<ATProtoCredentials>

    fun isAuthenticated(): Flow<Boolean>

    fun observeCredentials(): Flow<ATProtoCredentials?>

    suspend fun saveCredentials(credentials: ATProtoCredentials)

    suspend fun getStoredCredentials(): ATProtoCredentials?

    suspend fun logout()
}
