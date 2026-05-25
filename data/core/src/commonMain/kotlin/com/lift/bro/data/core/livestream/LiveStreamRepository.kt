package com.lift.bro.data.core.livestream

import kotlinx.coroutines.flow.Flow

interface LiveStreamRepository {

    fun isLive(
        url: String,
    ): Flow<Boolean>
}
