package com.lift.bro.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual object StoreManager {
    actual fun isUpdateAvailable(): Flow<Boolean> {
        return flowOf(false)
    }

    actual fun startUpdateFlow() {
    }
}
