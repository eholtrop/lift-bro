package com.lift.bro.presentation.dashboard

import com.lift.bro.domain.models.Category
import com.lift.bro.ui.card.lift.LiftCardState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DashboardReducerTest {

    private fun createLiftCard(
        name: String,
        maxWeight: Double? = null,
        maxReps: Double? = null,
        favourite: Boolean = false,
    ) = DashboardListItem.LiftCard.Loaded(
        state = LiftCardState(
            lift = Category(name = name),
            values = emptyList(),
            maxWeight = maxWeight,
            maxReps = maxReps,
            favourite = favourite,
        )
    )

    private fun loadedState(
        items: List<DashboardListItem>,
        sortingSettings: SortingSettings = SortingSettings(),
    ) = Loaded(items = items, sortingSettings = sortingSettings)

    @Test
    fun `Given AddLiftClicked When reducing Then state unchanged`() = runTest {
        val state = loadedState(items = listOf(createLiftCard("Bench")))

        val result = dashboardReducer(state, DashboardEvent.AddLiftClicked)

        assertEquals(state, result)
    }

    @Test
    fun `Given LiftClicked When reducing Then state unchanged`() = runTest {
        val state = loadedState(items = listOf(createLiftCard("Bench")))

        val result = dashboardReducer(state, DashboardEvent.LiftClicked("lift1"))

        assertEquals(state, result)
    }

    @Test
    fun `Given FavouritesAtTopToggled When reducing Then toggles favouritesAtTop`() = runTest {
        val state = loadedState(
            items = listOf(createLiftCard("Bench", favourite = true)),
            sortingSettings = SortingSettings(favouritesAtTop = true)
        )

        val result = dashboardReducer(state, DashboardEvent.FavouritesAtTopToggled) as Loaded

        assertFalse(result.sortingSettings.favouritesAtTop)
    }

    @Test
    fun `Given SortingOptionSelected When reducing Then updates sorting option`() = runTest {
        val state = loadedState(
            items = listOf(createLiftCard("Bench")),
            sortingSettings = SortingSettings(option = SortingOption.Name)
        )

        val result = dashboardReducer(state, DashboardEvent.SortingOptionSelected(SortingOption.Heaviest)) as Loaded

        assertEquals(SortingOption.Heaviest, result.sortingSettings.option)
    }

    @Test
    fun `Given SortingOptionSelected on Loading state When reducing Then returns Loading`() = runTest {
        val result = dashboardReducer(Loading, DashboardEvent.SortingOptionSelected(SortingOption.Heaviest))

        assertTrue(result is Loading)
    }

    @Test
    fun `Given favouritesAtTop and mixed cards When sorting Then favourites appear first`() = runTest {
        val favouriteCard = createLiftCard("Favourite", favourite = true)
        val nonFavouriteCard = createLiftCard("Normal", favourite = false)
        val state = loadedState(
            items = listOf(nonFavouriteCard, favouriteCard),
            sortingSettings = SortingSettings(favouritesAtTop = true)
        )

        val result = dashboardReducer(state, DashboardEvent.SortingOptionSelected(SortingOption.Name)) as Loaded

        assertTrue(result.items[0] is DashboardListItem.LiftCard.Loaded)
        assertTrue((result.items[0] as DashboardListItem.LiftCard.Loaded).state.favourite)
        assertFalse((result.items[1] as DashboardListItem.LiftCard.Loaded).state.favourite)
    }

    @Test
    fun `Given Heaviest sort When reducing Then sorts by maxWeight descending`() = runTest {
        val lightCard = createLiftCard("Light", maxWeight = 50.0)
        val heavyCard = createLiftCard("Heavy", maxWeight = 100.0)
        val state = loadedState(
            items = listOf(lightCard, heavyCard),
            sortingSettings = SortingSettings(option = SortingOption.Name, favouritesAtTop = false)
        )

        val result = dashboardReducer(state, DashboardEvent.SortingOptionSelected(SortingOption.Heaviest)) as Loaded

        assertEquals("Heavy", (result.items[0] as DashboardListItem.LiftCard.Loaded).state.lift.name)
        assertEquals("Light", (result.items[1] as DashboardListItem.LiftCard.Loaded).state.lift.name)
    }

    @Test
    fun `Given Reps sort When reducing Then sorts by maxReps descending`() = runTest {
        val lowRepsCard = createLiftCard("Low", maxReps = 5.0)
        val highRepsCard = createLiftCard("High", maxReps = 20.0)
        val state = loadedState(
            items = listOf(lowRepsCard, highRepsCard),
            sortingSettings = SortingSettings(option = SortingOption.Name, favouritesAtTop = false)
        )

        val result = dashboardReducer(state, DashboardEvent.SortingOptionSelected(SortingOption.Reps)) as Loaded

        assertEquals("High", (result.items[0] as DashboardListItem.LiftCard.Loaded).state.lift.name)
        assertEquals("Low", (result.items[1] as DashboardListItem.LiftCard.Loaded).state.lift.name)
    }

    @Test
    fun `Given Name sort When reducing Then sorts alphabetically`() = runTest {
        val zCard = createLiftCard("Zebra")
        val aCard = createLiftCard("Apple")
        val state = loadedState(
            items = listOf(zCard, aCard),
            sortingSettings = SortingSettings(option = SortingOption.Heaviest, favouritesAtTop = false)
        )

        val result = dashboardReducer(state, DashboardEvent.SortingOptionSelected(SortingOption.Name)) as Loaded

        assertEquals("Apple", (result.items[0] as DashboardListItem.LiftCard.Loaded).state.lift.name)
        assertEquals("Zebra", (result.items[1] as DashboardListItem.LiftCard.Loaded).state.lift.name)
    }
}
