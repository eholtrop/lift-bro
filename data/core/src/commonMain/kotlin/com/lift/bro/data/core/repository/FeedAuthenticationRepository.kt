package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.FeedAuthDataSource
import com.lift.bro.domain.feed.IFeedAuthenticationRepository
import com.lift.bro.domain.models.ATProtoCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FeedAuthenticationRepository(
    private val feedAuthDataSource: FeedAuthDataSource,
) : IFeedAuthenticationRepository {

    private val _credentials = MutableStateFlow<ATProtoCredentials?>(null)

    override suspend fun authenticate(
        handle: String,
        password: String,
    ): Result<ATProtoCredentials> {
        return feedAuthDataSource.createSession(handle, password).also { result ->
            result.getOrNull()?.let { credentials ->
                _credentials.value = credentials
            }
        }
    }

    override suspend fun startOAuth(pdsHost: String): Result<ATProtoCredentials> {
        return feedAuthDataSource.createSessionOAuth(pdsHost)
    }

    override suspend fun refreshSession(
        credentials: ATProtoCredentials,
    ): Result<ATProtoCredentials> {
        return feedAuthDataSource.refreshSession(credentials).also { result ->
            result.getOrNull()?.let { newCredentials ->
                _credentials.value = newCredentials
            }
        }
    }

    override fun isAuthenticated(): Flow<Boolean> = _credentials.map { it != null }

    override fun observeCredentials(): Flow<ATProtoCredentials?> = _credentials.asStateFlow()

    override suspend fun saveCredentials(credentials: ATProtoCredentials) {
        _credentials.value = credentials
    }

    override suspend fun getStoredCredentials(): ATProtoCredentials? {
        return _credentials.value
    }

    override suspend fun logout() {
        _credentials.value = null
    }
}
