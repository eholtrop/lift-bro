package com.lift.bro.data.core.datasource

interface LiveStreamDataSource {
    suspend fun isLive(channelName: String): Boolean
}
