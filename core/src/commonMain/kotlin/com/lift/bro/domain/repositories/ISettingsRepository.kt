package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Settings
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {

    fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight>

    fun saveUnitOfMeasure(uom: Settings.UnitOfWeight)
}