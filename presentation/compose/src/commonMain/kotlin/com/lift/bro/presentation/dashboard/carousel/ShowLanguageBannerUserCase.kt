package com.lift.bro.presentation.dashboard.carousel

import androidx.compose.ui.text.intl.Locale
import com.lift.bro.config.BuildConfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import com.lift.bro.presentation.settings.isAiGen
import com.lift.bro.presentation.settings.supportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ShowLanguageBannerUserCase(
    private val settingsRepository: ISettingsRepository = dependencies.settingsRepository,
) {

    operator fun invoke(): Flow<Boolean> = combine(
        settingsRepository.listen(Setting.AITranslationBannerDismissed),
        settingsRepository.listen(Setting.LocaleOverride),
    ) { bannerDismissed, overrideLocale ->
        !bannerDismissed &&
            (
                Locale.current.language.supportedLanguage()?.isAiGen() == true ||
                    overrideLocale != null ||
                    BuildConfig.isDebug
                )
    }
}
