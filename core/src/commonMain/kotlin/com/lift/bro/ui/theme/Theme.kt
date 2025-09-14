package com.example.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.lift.bro.di.dependencies
import com.lift.bro.ui.navigation.LocalSnackbarHostState
import lift_bro.core.generated.resources.DMSans_Black
import lift_bro.core.generated.resources.DMSans_Bold
import lift_bro.core.generated.resources.DMSans_ExtraBold
import lift_bro.core.generated.resources.DMSans_ExtraLight
import lift_bro.core.generated.resources.DMSans_Light
import lift_bro.core.generated.resources.DMSans_Medium
import lift_bro.core.generated.resources.DMSans_Regular
import lift_bro.core.generated.resources.DMSans_SemiBold
import lift_bro.core.generated.resources.DMSans_Thin
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.provicali
import org.jetbrains.compose.resources.Font


private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_tertiary,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_tertiary,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

enum class ThemeMode {
    Light, Dark, System
}


@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val themeMode by dependencies.settingsRepository.getThemeMode().collectAsState(ThemeMode.System)

    CompositionLocalProvider(
        LocalSnackbarHostState provides remember { SnackbarHostState() }
    ) {
        MaterialTheme(
            colorScheme = when (themeMode) {
                ThemeMode.Light ->
                    LightColors.copy(
                        surface = LightColors.surfaceDim,
                        surfaceDim = LightColors.surfaceDim.copy(alpha = .4f)
                    )
                ThemeMode.Dark -> {
                    DarkColors.copy(
                        surface = DarkColors.surfaceVariant,
                        surfaceDim = DarkColors.surfaceVariant.copy(alpha = .4f)
                    )
                }
                ThemeMode.System -> {
                    if (useDarkTheme) {
                        DarkColors.copy(
                            surface = DarkColors.surfaceVariant,
                            surfaceDim = DarkColors.surfaceVariant.copy(alpha = .4f)
                        )
                    } else {
                        LightColors.copy(
                            surface = LightColors.surfaceDim,
                            surfaceDim = LightColors.surfaceDim.copy(alpha = .4f)
                        )
                    }
                }
            },
            content = {
                Box(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.background
                    )
                ) {
                    content()
                }
            },
            typography = typography()
        )
    }
}

@Composable
private fun typography() = Typography(
    displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = provicali()),
    displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = provicali()),
    displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = provicali()),
    headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = provicali()),
    headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = provicali()),
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = provicali()),
    titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = dmSansFontFamily(), fontWeight = FontWeight.Bold),
    titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = dmSansFontFamily(), fontWeight = FontWeight.Bold),
    titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = dmSansFontFamily(), fontWeight = FontWeight.Bold),
    bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = dmSansFontFamily()),
    bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = dmSansFontFamily()),
    bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = dmSansFontFamily()),
    labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = dmSansFontFamily()),
    labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = dmSansFontFamily()),
    labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = dmSansFontFamily()),

)

@Composable
private fun provicali() = FontFamily(
    Font(Res.font.provicali, weight = FontWeight.Normal, style = FontStyle.Normal)
)

@Composable
private fun dmSansFontFamily() = FontFamily(
    Font(Res.font.DMSans_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.DMSans_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.DMSans_Bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.DMSans_Medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.DMSans_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.DMSans_Thin, FontWeight.Thin, FontStyle.Normal),
    Font(Res.font.DMSans_Black, FontWeight.Black, FontStyle.Normal),
    Font(Res.font.DMSans_Light, FontWeight.Light, FontStyle.Normal),
    Font(Res.font.DMSans_ExtraLight, FontWeight.ExtraLight, FontStyle.Normal),
)