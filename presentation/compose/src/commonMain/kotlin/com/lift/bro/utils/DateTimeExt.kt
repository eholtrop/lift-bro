package com.lift.bro.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

val Clock.System.today get() = Clock.System.todayIn(TimeZone.currentSystemDefault())
