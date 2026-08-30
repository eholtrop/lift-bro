package com.lift.bro.presentation

import kotlinx.coroutines.flow.MutableStateFlow

object DeepLinkState {
    val url = MutableStateFlow<String?>(null)
}
