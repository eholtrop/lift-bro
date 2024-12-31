package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Day
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface IDayRepository {

    fun listen(date: LocalDate): Flow<Day>

    fun listenAll(): Flow<List<Day>>

    fun save(day: Day)
}