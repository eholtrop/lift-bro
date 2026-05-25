package com.lift.bro.data.core.livestream

import com.lift.bro.data.core.datasource.LiveStreamDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LiveStreamRepositoryImpl(
    private val dataSource: LiveStreamDataSource,
): LiveStreamRepository {

    override fun isLive(url: String): Flow<Boolean> = flow {
        val channelName = requireNotNull(parseChannelName(url)) { "Invalid Twitch URL: $url" }
        emit(
            try {
                dataSource.isLive(channelName)
            } catch (_: Exception) {
                false
            }
        )
    }
}

private fun parseChannelName(url: String): String? {
    return url
        .removePrefix("https://").removePrefix("http://")
        .removePrefix("www.")
        .removePrefix("twitch.tv/")
        .substringBefore("?").substringBefore("/")
        .ifBlank { null }
}
