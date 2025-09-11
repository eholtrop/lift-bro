package com.lift.bro.data.datasource

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

fun <T: Any> Query<T>.flowToList(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Flow<List<T>> = this.asFlow().mapToList(dispatcher)
    .flowOn(dispatcher)


fun <T: Any> Query<T>.flowToOneOrNull(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Flow<T?> = this.asFlow().mapToOneOrNull(dispatcher)
    .flowOn(dispatcher)