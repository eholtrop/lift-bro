package com.lift.bro

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.lift.bro.presentation.goals.GoalsScreenPreview
import com.lift.bro.presentation.home.EmptyHomeScreenPreview
import com.lift.bro.presentation.home.HomeScreenContentPreview
import com.lift.bro.presentation.home.HomeState
import com.lift.bro.presentation.home.Tab
import com.lift.bro.presentation.onboarding.OnboardingBroScreenPreview
import com.lift.bro.presentation.onboarding.OnboardingConsentScreenPreview
import com.lift.bro.presentation.onboarding.OnboardingSkipScreenPreview
import com.lift.bro.presentation.set.RepWeightSelectorPreview
import com.lift.bro.presentation.settings.BackupSettingsRowPreview
import com.lift.bro.presentation.settings.MERSettingsRowPreview
import com.lift.bro.presentation.settings.SettingsRowItemPreview
import com.lift.bro.presentation.settings.TWMSettingsRowPreview
import com.lift.bro.presentation.settings.ThemeSettingsRowPreview
import com.lift.bro.presentation.settings.UOMSettingsRowPreview
import com.lift.bro.presentation.settings.eMaxSettingsRowPreview
import com.lift.bro.presentation.settings.server.ServerSettingsRowPreview
import com.lift.bro.presentation.workout.RecentWorkoutCardPreview
import com.lift.bro.presentation.wrapped.WrappedConsistencyScreenPreview
import com.lift.bro.presentation.wrapped.WrappedLandingScreenPreview
import com.lift.bro.presentation.wrapped.goals.WrappedGoalsScreenPreview
import com.lift.bro.presentation.wrapped.progress.WrappedProgressScreenPreview
import com.lift.bro.presentation.wrapped.summary.WrappedSummaryScreenPreview
import com.lift.bro.ui.AnimatedRotatingTextPreview
import com.lift.bro.ui.AnimatedTextPreview
import com.lift.bro.ui.CardPreview
import com.lift.bro.ui.DecimalPickerPreview
import com.lift.bro.ui.DropDownButtonPreview
import com.lift.bro.ui.LineItemPreview
import com.lift.bro.ui.NumberPickerPreview
import com.lift.bro.ui.RadioButtonCardPreview
import com.lift.bro.ui.RadioFieldPreview
import com.lift.bro.ui.RpeSelectorPreview
import com.lift.bro.ui.SetInfoRowPreview
import com.lift.bro.ui.TempoSelectorPreview
import com.lift.bro.ui.TopBarButtonPreview
import com.lift.bro.ui.TopBarIconButtonPreview
import com.lift.bro.ui.calendar.CalendarDatePreview
import com.lift.bro.ui.calendar.CalendarMonthPreview
import com.lift.bro.ui.calendar.CalendarPreview
import com.lift.bro.ui.card.lift.LiftCardEmptyPreview
import com.lift.bro.ui.card.lift.LiftCardRepsPreview
import com.lift.bro.ui.card.lift.LiftCardWeightPreview
import com.lift.bro.ui.dialog.InfoDialogButtonPreview

/**
 * Screenshot test wrapper class that bridges existing @Preview functions
 * for the Google Compose Screenshot Testing plugin.
 *
 * Each test function wraps an existing preview, providing both light and dark mode variants.
 */
class LiftBroScreenshots {

    // ==================== Home Screens ====================

    @PreviewTest
    @Composable
    fun HomeScreenLoadingLight() {
        HomeScreenContentPreview(HomeState.Loading)
    }

    @PreviewTest
    @Composable
    fun HomeScreenEmptyLight() {
        HomeScreenContentPreview(HomeState.Empty)
    }

    @PreviewTest
    @Composable
    fun HomeScreenDashboardNoGoalsLight() {
        HomeScreenContentPreview(
            HomeState.Content(
                selectedTab = Tab.Dashboard,
                goals = emptyList()
            )
        )
    }

    @PreviewTest
    @Composable
    fun HomeScreenDashboardWithGoalsLight() {
        HomeScreenContentPreview(
            HomeState.Content(
                selectedTab = Tab.Dashboard,
                goals = listOf("Squat 405 lbs", "Bench 315 lbs", "Deadlift 500 lbs")
            )
        )
    }

    @PreviewTest
    @Composable
    fun HomeScreenCalendarLight() {
        HomeScreenContentPreview(
            HomeState.Content(
                selectedTab = Tab.WorkoutCalendar,
                goals = listOf("Consistency: 4x per week")
            )
        )
    }

    @PreviewTest
    @Composable
    fun EmptyHomeScreenLight() {
        EmptyHomeScreenPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun EmptyHomeScreenDark() {
        EmptyHomeScreenPreview(darkMode = true)
    }

    // ==================== Onboarding Screens ====================

    @PreviewTest
    @Composable
    fun OnboardingBroScreenLight() {
        OnboardingBroScreenPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun OnboardingBroScreenDark() {
        OnboardingBroScreenPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun OnboardingConsentScreenLight() {
        OnboardingConsentScreenPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun OnboardingConsentScreenDark() {
        OnboardingConsentScreenPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun OnboardingSkipScreenLight() {
        OnboardingSkipScreenPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun OnboardingSkipScreenDark() {
        OnboardingSkipScreenPreview(darkMode = true)
    }

    // ==================== Settings Screens ====================

    @PreviewTest
    @Composable
    fun SettingsRowItemLight() {
        SettingsRowItemPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun SettingsRowItemDark() {
        SettingsRowItemPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun BackupSettingsRowLight() {
        BackupSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun BackupSettingsRowDark() {
        BackupSettingsRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun ThemeSettingsRowLight() {
        ThemeSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun ThemeSettingsRowDark() {
        ThemeSettingsRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun UOMSettingsRowLight() {
        UOMSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun UOMSettingsRowDark() {
        UOMSettingsRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun MERSettingsRowLight() {
        MERSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun MERSettingsRowDark() {
        MERSettingsRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun TWMSettingsRowLight() {
        TWMSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun TWMSettingsRowDark() {
        TWMSettingsRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun EMaxSettingsRowLight() {
        eMaxSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun EMaxSettingsRowDark() {
        eMaxSettingsRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun ServerSettingsRowLight() {
        ServerSettingsRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun ServerSettingsRowDark() {
        ServerSettingsRowPreview(darkMode = true)
    }

    // ==================== UI Components ====================

    @PreviewTest
    @Composable
    fun CardLight() {
        CardPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun CardDark() {
        CardPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun TopBarIconButtonLight() {
        TopBarIconButtonPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun TopBarIconButtonDark() {
        TopBarIconButtonPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun TopBarButtonLight() {
        TopBarButtonPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun TopBarButtonDark() {
        TopBarButtonPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun RadioFieldLight() {
        RadioFieldPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun RadioFieldDark() {
        RadioFieldPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun RadioButtonCardLight() {
        RadioButtonCardPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun RadioButtonCardDark() {
        RadioButtonCardPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun DropDownButtonLight() {
        DropDownButtonPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun DropDownButtonDark() {
        DropDownButtonPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun NumberPickerLight() {
        NumberPickerPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun NumberPickerDark() {
        NumberPickerPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun DecimalPickerLight() {
        DecimalPickerPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun DecimalPickerDark() {
        DecimalPickerPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun AnimatedTextLight() {
        AnimatedTextPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun AnimatedTextDark() {
        AnimatedTextPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun RotatingAnimatedTextLight() {
        AnimatedRotatingTextPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun RotatingAnimatedTextDark() {
        AnimatedRotatingTextPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun RpeSelectorLight() {
        RpeSelectorPreview(isDarkMode = false)
    }

    @PreviewTest
    @Composable
    fun RpeSelectorDark() {
        RpeSelectorPreview(isDarkMode = true)
    }

    @PreviewTest
    @Composable
    fun TempoSelectorLight() {
        TempoSelectorPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun TempoSelectorDark() {
        TempoSelectorPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun SetInfoRowLight() {
        SetInfoRowPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun SetInfoRowDark() {
        SetInfoRowPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun LineItemLight() {
        LineItemPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun LineItemDark() {
        LineItemPreview(darkMode = true)
    }

    // ==================== Calendar Components ====================

    @PreviewTest
    @Composable
    fun CalendarLight() {
        CalendarPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun CalendarDark() {
        CalendarPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun CalendarDateLight() {
        CalendarDatePreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun CalendarDateDark() {
        CalendarDatePreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun CalendarMonthLight() {
        CalendarMonthPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun CalendarMonthDark() {
        CalendarMonthPreview(darkMode = true)
    }

    // ==================== Lift Card Components ====================

    @PreviewTest
    @Composable
    fun LiftCardEmptyLight() {
        LiftCardEmptyPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun LiftCardEmptyDark() {
        LiftCardEmptyPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun LiftCardRepsLight() {
        LiftCardRepsPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun LiftCardRepsDark() {
        LiftCardRepsPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun LiftCardWeightLight() {
        LiftCardWeightPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun LiftCardWeightDark() {
        LiftCardWeightPreview(darkMode = true)
    }


    // ==================== Dialog Components ====================

    @PreviewTest
    @Composable
    fun InfoDialogLight() {
        InfoDialogButtonPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun InfoDialogDark() {
        InfoDialogButtonPreview(darkMode = true)
    }

    // ==================== Workout Screens ====================

    @PreviewTest
    @Composable
    fun RecentWorkoutCardLight() {
        RecentWorkoutCardPreview(isDark = false)
    }

    @PreviewTest
    @Composable
    fun RecentWorkoutCardDark() {
        RecentWorkoutCardPreview(isDark = true)
    }

    // ==================== Set Screens ====================

    @PreviewTest
    @Composable
    fun RepWeightSelectorLight() {
        RepWeightSelectorPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun RepWeightSelectorDark() {
        RepWeightSelectorPreview(darkMode = true)
    }


    // ==================== Goals Screens ====================

    @PreviewTest
    @Composable
    fun GoalsScreenLight() {
        GoalsScreenPreview(mode = false)
    }

    @PreviewTest
    @Composable
    fun GoalsScreenDark() {
        GoalsScreenPreview(mode = true)
    }

    // ==================== Wrapped Screens ====================

    @PreviewTest
    @Composable
    fun WrappedLandingScreenLight() {
        WrappedLandingScreenPreview(darkMode = false)
    }

    @PreviewTest
    @Composable
    fun WrappedLandingScreenDark() {
        WrappedLandingScreenPreview(darkMode = true)
    }

    @PreviewTest
    @Composable
    fun WrappedConsistencyScreenLight() {
        WrappedConsistencyScreenPreview(dark = false)
    }

    @PreviewTest
    @Composable
    fun WrappedConsistencyScreenDark() {
        WrappedConsistencyScreenPreview(dark = true)
    }

    @PreviewTest
    @Composable
    fun WrappedGoalsScreenLight() {
        WrappedGoalsScreenPreview(dark = false)
    }

    @PreviewTest
    @Composable
    fun WrappedGoalsScreenDark() {
        WrappedGoalsScreenPreview(dark = true)
    }

    @PreviewTest
    @Composable
    fun WrappedProgressScreenLight() {
        WrappedProgressScreenPreview(dark = false)
    }

    @PreviewTest
    @Composable
    fun WrappedProgressScreenDark() {
        WrappedProgressScreenPreview(dark = true)
    }

    @PreviewTest
    @Composable
    fun WrappedSummaryScreenLight() {
        WrappedSummaryScreenPreview(dark = false)
    }

    @PreviewTest
    @Composable
    fun WrappedSummaryScreenDark() {
        WrappedSummaryScreenPreview(dark = true)
    }
}
