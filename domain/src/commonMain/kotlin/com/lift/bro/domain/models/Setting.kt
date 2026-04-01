package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Setting<T> {
    @Serializable
    data object TimerEnabled : Setting<Boolean>()

    @Serializable
    data object EditSetVersion : Setting<Int>()

    @Serializable
    data object DashboardV3 : Setting<Boolean>()

    @Serializable
    data object DeviceId : Setting<String>()

    @Serializable
    data object DeviceConsent : Setting<Consent?>()

    @Serializable
    data object UnitOfMeasure : Setting<Settings.UnitOfWeight>()

    @Serializable
    data object DeviceFtux : Setting<Boolean>()

    @Serializable
    data object BackupSettingsKey : Setting<BackupSettings>()

    @Serializable
    data object Bro : Setting<LiftBro?>()

    @Serializable
    data object MerSettings : Setting<MERSettings>()

    @Serializable
    data object LatestReadReleaseNotes : Setting<String?>()

    @Serializable
    data object ThemeModeKey : Setting<ThemeMode>()

    @Serializable
    data object EMaxEnabled : Setting<Boolean>()

    @Serializable
    data object TMaxEnabled : Setting<Boolean>()

    @Serializable
    data object ClientUrl : Setting<String?>()

    @Serializable
    data object ShowTotalWeightMoved : Setting<Boolean>()
}
