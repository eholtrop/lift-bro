package com.lift.bro.domain.usecases

import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.repositories.Consent
import com.lift.bro.domain.repositories.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ConsentDeviceUseCase(
    val settingsRepository: ISettingsRepository
) {
    operator fun invoke() {
        settingsRepository.setDeviceConsent(
            Consent(
                deviceId = settingsRepository.getDeviceId(),
                appVersion = BuildKonfig.VERSION_NAME,
                tncVersion = 1.0,
                privacyPolicyVersion = 1.0,
                consentDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            )
        )
    }
}

class HasDeviceConsentedUseCase(
    val settingsRepository: ISettingsRepository
) {

    operator fun invoke(): Flow<Boolean> = settingsRepository.getDeviceConsent()
        .map { it != null }
}