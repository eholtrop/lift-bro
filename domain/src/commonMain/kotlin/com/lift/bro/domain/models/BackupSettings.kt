package com.lift.bro.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Consent(
    val deviceId: String,
    val consentDateTime: kotlinx.datetime.LocalDateTime,
    val tncVersion: Double,
    val privacyPolicyVersion: Double,
    val appVersion: String
)

data class BackupSettings(
    val lastBackupDate: LocalDate,
)
