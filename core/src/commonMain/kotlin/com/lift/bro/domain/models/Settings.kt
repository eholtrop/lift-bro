package com.lift.bro.domain.models

import com.lift.bro.presentation.variation.UOM
import kotlin.jvm.JvmInline

sealed interface Settings {

    @JvmInline
    value class UnitOfWeight(val uom: UOM): Settings
}