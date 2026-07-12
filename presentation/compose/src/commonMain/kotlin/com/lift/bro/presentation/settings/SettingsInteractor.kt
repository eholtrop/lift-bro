package com.lift.bro.presentation.settings

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.rememberInteractor

@Serializable
data class SettingsState(
    val selectedTab: SettingsTab = SettingsTab.Profile,
    private val items: List<Setting<*>> = selectedTab.settings(),
) {
    val experimentalSettings = items.filter { it.isExperimental }

    val settings = items.filter { !it.isPro && !it.isExperimental }

    val proSettings = items.filter { it.isPro }
}

private val Setting<*>.isPro
    get() = when (this) {
        Setting.MerSettings -> true
        Setting.ShowTotalWeightMoved -> true
        Setting.EMaxEnabled -> true
        Setting.TMaxEnabled -> true
        else -> false
    }
private val Setting<*>.isExperimental
    get() = when (this) {
            Setting.Timer -> true
            Setting.DashboardV3 -> true
            else -> false
        }


sealed interface SettingsEvent {
    data object ProfileTabSelected: SettingsEvent
    data object ApplicationTabSelected: SettingsEvent
}

typealias SettingsInteractor = Interactor<SettingsState, SettingsEvent>

@Composable
fun rememberSettingsInteractor(): SettingsInteractor = rememberInteractor(
    initialState = SettingsState(),
    reducers = listOf(
        Reducer { state, event ->
            when (event) {
                SettingsEvent.ApplicationTabSelected -> state.copy(
                    selectedTab = SettingsTab.Application,
                    items = SettingsTab.Application.settings()
                )

                SettingsEvent.ProfileTabSelected -> state.copy(
                    selectedTab = SettingsTab.Profile,
                    items = SettingsTab.Profile.settings()
                )
            }
        }
    )
)

private fun SettingsTab.settings(): List<Setting<*>> = when (this) {
    SettingsTab.Profile -> listOf(
        Setting.BackupSettings,
        Setting.LocaleOverride,
        Setting.ThemeMode,
        Setting.MerSettings,
        Setting.ShowTotalWeightMoved,
        Setting.EMaxEnabled,
    )

    SettingsTab.Application -> listOf(
        Setting.ClientUrl,
        Setting.Timer,
        Setting.DashboardV3,
    )
}
