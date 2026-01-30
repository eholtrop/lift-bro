package tv.dpal.ktx.datetime

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

val Clock.System.today get() = Clock.System.todayIn(TimeZone.currentSystemDefault())
