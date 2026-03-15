package com.lift.bro.presentation.settings

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.Setting
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import tv.dpal.ext.flow.combine
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor

typealias SettingsInteractor = Interactor<SettingsState, SettingsEvent>

@Serializable
data class SettingsState(
    val items: List<SettingsItem> = emptyList(),
    val proItems: List<SettingsItem> = emptyList(),
    val experimental: List<SettingsItem> = emptyList(),
)

@Serializable
sealed interface SettingsItem {
    @Serializable
    data object Backup: SettingsItem

    @Serializable
    data class Bro(val bro: LiftBro): SettingsItem

    @Serializable
    data class DbLocation(val clientUrl: String?): SettingsItem

    @Serializable
    data class DashboardV3(val enabled: Boolean): SettingsItem

    @Serializable
    data class MaxAppearance(
        val eMaxEnabled: Boolean = false,
        val tMaxEnabled: Boolean = false,
    ): SettingsItem

    @Serializable
    data class ShowTWM(val enabled: Boolean): SettingsItem

    @Serializable
    data class MerSettings(val enabled: Boolean): SettingsItem

    @Serializable
    data class AppTheme(val mode: ThemeMode): SettingsItem

    @Serializable
    data class Timer(val enabled: Boolean): SettingsItem

    @Serializable
    data class LocalServer(val enabled: Boolean): SettingsItem
}

sealed interface SettingsEvent {
    data class SaveSetting(val setting: Setting<Any>, val value: Any): SettingsEvent
}

@Composable
fun rememberSettingsInteractor(
    pro: Boolean = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro,
    settingsRepository: ISettingsRepository = dependencies.settingsRepository,
): SettingsInteractor = rememberInteractor(
    initialState = SettingsState(),
    source = {
        combine(
            settingsRepository.listen(Setting.Bro),
            settingsRepository.listen(Setting.ClientUrl),
            settingsRepository.listen(Setting.DashboardV3),
            combine(
                settingsRepository.listen(Setting.EMaxEnabled),
                settingsRepository.listen(Setting.TMaxEnabled),
            ) { emax, tmax ->
                SettingsItem.MaxAppearance(emax, tmax)
            },
            settingsRepository.listen(Setting.MerSettings),
            settingsRepository.listen(Setting.ShowTotalWeightMoved),
            settingsRepository.listen(Setting.ThemeModeKey),
            settingsRepository.listen(Setting.TimerEnabled),
        ) { bro, clientUrl, dashboardV3, maxAppearance, mer, twm, theme, timer ->
            SettingsState(
                items = listOf(
                    SettingsItem.Bro(bro = bro ?: LiftBro.entries.random()),
                    SettingsItem.Backup,
                    SettingsItem.AppTheme(mode = theme),
                    SettingsItem.DbLocation(clientUrl = clientUrl),
                ),
                proItems = if (pro) {
                    listOf(
                        SettingsItem.ShowTWM(enabled = twm),
                        SettingsItem.MerSettings(enabled = mer.enabled),
                        maxAppearance,
                    )
                } else {
                    emptyList()
                },
                experimental = if (pro) {
                    listOf(
                        SettingsItem.Timer(enabled = timer),
                        SettingsItem.DashboardV3(enabled = dashboardV3),
                    )
                } else {
                    emptyList()
                }
            )
        }
    },
    sideEffects = listOf(
        SideEffect { _, _, event ->
            when (event) {
                is SettingsEvent.SaveSetting -> {
                    settingsRepository.set(event.setting, event.value)
                }
            }
        }
    )
)
