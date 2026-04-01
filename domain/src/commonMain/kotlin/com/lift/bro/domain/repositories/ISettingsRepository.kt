package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Setting
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun <T> get(setting: Setting<T>): T

    fun <T> set(
        setting: Setting<T>,
        value: T,
    )

    fun <T> listen(setting: Setting<T>): Flow<T>
}
