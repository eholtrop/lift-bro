package com.lift.bro.domain.usecases

import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.models.Consent
import com.lift.bro.domain.models.Setting
import com.lift.bro.domain.repositories.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ConsentDeviceUseCase(
    val settingsRepository: ISettingsRepository,
) {
    operator fun invoke() {
        settingsRepository.set(
            Setting.DeviceConsent,
            Consent(
                deviceId = settingsRepository.get(Setting.DeviceId),
                appVersion = BuildKonfig.VERSION_NAME,
                tncVersion = 1.0,
                privacyPolicyVersion = 1.0,
                consentDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC),
            ),
        )
    }
}

class HasDeviceConsentedUseCase(
    val settingsRepository: ISettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        settingsRepository.listen(Setting.DeviceConsent)
            .map { it != null }
}
