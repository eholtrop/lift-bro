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
import kotlinx.coroutines.flow.combine as flowCombine

/*
 * A debug utility function for flows, outputting events as they trigger
 */
fun <T> Flow<T>.debug(tag: String = "FlowDebug"): Flow<T> = this
    .onEach { Log.d(tag, it.toString()) }
    .onStart { Log.d(tag, "onStart") }
    .onCompletion { Log.d(tag, "onComplete") }
    .catch { Log.d(tag, it.message ?: "") }
    .onEmpty { Log.d(tag, "empty") }

/**
 * Maps each item in the list based on the transform function provided
 */
public fun <T, R> Flow<List<T>>.mapEach(transform: (T) -> R): Flow<List<R>> = this.map { it.map(transform) }

/**
 * Filters each item in the list based on the filter provided
 */
public fun <T> Flow<List<T>>.filterEach(filter: (T) -> Boolean): Flow<List<T>> = this.map { it.filter(filter) }

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return flowCombine(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6
    ) { arr ->
        transform(
            arr[0] as T1,
            arr[1] as T2,
            arr[2] as T3,
            arr[3] as T4,
            arr[4] as T5,
            arr[5] as T6,
        )
    }
}
fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> {
    return flowCombine(
        flow1,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6,
        flow7,
    ) { arr ->
        transform(
            arr[0] as T1,
            arr[1] as T2,
            arr[2] as T3,
            arr[3] as T4,
            arr[4] as T5,
            arr[5] as T6,
            arr[6] as T7
        )
    }
}
