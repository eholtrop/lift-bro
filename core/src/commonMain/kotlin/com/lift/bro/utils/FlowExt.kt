package com.lift.bro.utils

import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart

fun <T> Flow<T>.debug(tag: String = "FlowDebug"): Flow<T> = this
    .onEach { Log.d(tag, it.toString()) }
    .onStart { Log.d(tag, "onStart") }
    .onCompletion { Log.d(tag, "onComplete") }
    .catch { Log.d(tag, it.message ?: "" ) }
    .onEmpty { Log.d(tag, "empty") }

public fun <T, R> Flow<List<T>>.mapEach(transform: (T) -> R): Flow<List<R>> = this.map { it.map(transform) }