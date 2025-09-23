package com.lift.bro.ui.saver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.datetime.LocalDate

val LocalDateSaver = object : Saver<LocalDate, String> {

    override fun SaverScope.save(value: LocalDate): String {
        return value.toString() // LocalDate.toString() produces ISO 8601 format (YYYY-MM-DD)
    }

    override fun restore(value: String): LocalDate {
        return LocalDate.parse(value)
    }
}

val MutableLocalDateSaver = object : Saver<MutableState<LocalDate>, String> {

    override fun SaverScope.save(value: MutableState<LocalDate>): String {
        return value.value.toString()
    }

    override fun restore(value: String): MutableState<LocalDate> {
        return mutableStateOf(LocalDate.parse(value))
    }
}