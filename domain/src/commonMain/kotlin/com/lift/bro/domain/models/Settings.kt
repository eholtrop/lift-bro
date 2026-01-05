package com.lift.bro.domain.models

import kotlin.jvm.JvmInline

sealed interface Settings {

    @JvmInline
    value class UnitOfWeight(val uom: UOM) : Settings
}
