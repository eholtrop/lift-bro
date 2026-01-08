package com.lift.bro.domain.usecases

import com.lift.bro.domain.repositories.Consent
import com.lift.bro.domain.repositories.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConsentDeviceUseCaseTest {

    @Test
    fun `ConsentDeviceUseCase sets device consent with correct values`() = runTest {
        val fakeRepo = FakeSettingsRepository()
        val useCase = ConsentDeviceUseCase(fakeRepo)

        useCase()

        val consent = fakeRepo.capturedConsent
        assertNotNull(consent)
        assertEquals("test-device-123", consent.deviceId)
        assertEquals(1.0, consent.tncVersion)
        assertEquals(1.0, consent.privacyPolicyVersion)
        assertNotNull(consent.appVersion)
        assertNotNull(consent.consentDateTime)
    }

    @Test
    fun `HasDeviceConsentedUseCase returns true when consent exists`() = runTest {
        val consent = Consent(
            deviceId = "device-123",
            consentDateTime = LocalDateTime(2024, 1, 1, 0, 0),
            tncVersion = 1.0,
            privacyPolicyVersion = 1.0,
            appVersion = "1.0.0"
        )
        val fakeRepo = FakeSettingsRepository(existingConsent = consent)
        val useCase = HasDeviceConsentedUseCase(fakeRepo)

        val result = useCase().first()

        assertTrue(result)
    }

    @Test
    fun `HasDeviceConsentedUseCase returns false when consent is null`() = runTest {
        val fakeRepo = FakeSettingsRepository(existingConsent = null)
        val useCase = HasDeviceConsentedUseCase(fakeRepo)

        val result = useCase().first()

        assertFalse(result)
    }

    // Fake repository implementation for testing
    private class FakeSettingsRepository(
        private val existingConsent: Consent? = null
    ) : ISettingsRepository {
        var capturedConsent: Consent? = null
            private set

        override fun getDeviceId(): String = "test-device-123"

        override fun getDeviceConsent(): Flow<Consent?> = flowOf(existingConsent ?: capturedConsent)

        override fun setDeviceConsent(consent: Consent) {
            capturedConsent = consent
        }

        // Stub implementations for other methods
        override fun getUnitOfMeasure(): Flow<com.lift.bro.domain.models.Settings.UnitOfWeight> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun saveUnitOfMeasure(uom: com.lift.bro.domain.models.Settings.UnitOfWeight) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getDeviceFtux(): Flow<Boolean> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setDeviceFtux(ftux: Boolean) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getBackupSettings(): Flow<com.lift.bro.domain.repositories.BackupSettings> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun saveBackupSettings(settings: com.lift.bro.domain.repositories.BackupSettings) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getBro(): Flow<com.lift.bro.domain.models.LiftBro?> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setBro(bro: com.lift.bro.domain.models.LiftBro) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getMerSettings(): Flow<com.lift.bro.domain.models.MERSettings> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setMerSettings(merSettings: com.lift.bro.domain.models.MERSettings) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun showTotalWeightMoved(show: Boolean) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun shouldShowTotalWeightMoved(): Flow<Boolean> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getLatestReadReleaseNotes(): Flow<String?> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setLatestReadReleaseNotes(versionId: String) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getThemeMode(): Flow<com.lift.bro.domain.models.ThemeMode> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setThemeMode(themeMode: com.lift.bro.domain.models.ThemeMode) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun eMaxEnabled(): Flow<Boolean> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setEMaxEnabled(enabled: Boolean) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun tMaxEnabled(): Flow<Boolean> {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun setTMaxEnabled(enabled: Boolean) {
            throw NotImplementedError("Not needed for these tests")
        }

        override fun getClientUrl(): String? {
            TODO("Not yet implemented")
        }

        override fun setClientUrl(url: String?) {
            TODO("Not yet implemented")
        }
    }
}
