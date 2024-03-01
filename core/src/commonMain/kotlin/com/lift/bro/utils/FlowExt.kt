package com.lift.bro.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public fun <T, R> Flow<List<T>>.mapEach(transform: (T) -> R): Flow<List<R>> = this.map { it.map(transform) }