package com.lift.bro.presentation

import kotlinx.coroutines.flow.Flow

expect object StoreManager {

    fun isUpdateAvailable(): Flow<Boolean>

    fun startUpdateFlow()
}
