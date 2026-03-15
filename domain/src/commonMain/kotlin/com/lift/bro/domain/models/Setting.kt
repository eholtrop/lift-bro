package com.lift.bro.domain.models

sealed class Setting<T> {
    data object TimerEnabled : Setting<Boolean>()
    data object EditSetVersion : Setting<Int>()
    data object DeviceId : Setting<String>()
    data object DeviceConsent : Setting<Consent?>()
    data object UnitOfMeasure : Setting<Settings.UnitOfWeight>()
    data object DeviceFtux : Setting<Boolean>()
    data object BackupSettingsKey : Setting<BackupSettings>()
    data object Bro : Setting<LiftBro?>()
    data object MerSettings : Setting<MERSettings>()
    data object LatestReadReleaseNotes : Setting<String?>()
    data object ThemeModeKey : Setting<ThemeMode>()
    data object EMaxEnabled : Setting<Boolean>()
    data object TMaxEnabled : Setting<Boolean>()
    data object ClientUrl : Setting<String?>()
    data object ShowTotalWeightMoved : Setting<Boolean>()
}
