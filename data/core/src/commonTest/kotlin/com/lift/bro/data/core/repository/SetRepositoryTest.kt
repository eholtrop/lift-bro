package com.lift.bro.data.core.repository

import com.lift.bro.data.core.testdoubles.fakeSetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SetRepositoryTest {

    @Test
    fun `listenAll delegates to data source with parameters`() = runTest {
        val sets = listOf(
            LBSet(id = "1", movementId = "v1", weight = 100.0, reps = 5)
        )
        val dataSource = fakeSetDataSource(sets = sets)
        val repository = SetRepository(dataSource)
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)

        val result = repository.listenAll(
            startDate = startDate,
            endDate = endDate,
            variationId = "v1",
            reps = 5L,
            limit = 10L,
            sorting = Sorting.weight,
            order = Order.Ascending
        ).first()

        assertEquals(sets, result)
        assertEquals(startDate, dataSource.lastStartDate)
        assertEquals(endDate, dataSource.lastEndDate)
        assertEquals("v1", dataSource.lastVariationId)
        assertEquals(5L, dataSource.lastReps)
        assertEquals(10L, dataSource.lastLimit)
        assertEquals(Sorting.weight, dataSource.lastSorting)
        assertEquals(Order.Ascending, dataSource.lastOrder)
    }

    @Test
    fun `listenAllForLift delegates to data source`() = runTest {
        val sets = listOf(
            LBSet(id = "1", movementId = "v1", weight = 100.0, reps = 5)
        )
        val dataSource = fakeSetDataSource(sets = sets)
        val repository = SetRepository(dataSource)

        val result = repository.listenAllForLift(
            liftId = "lift1",
            startDate = null,
            endDate = null,
            limit = 50L,
            sorting = Sorting.date
        ).first()

        assertEquals(sets, result)
        assertEquals("lift1", dataSource.lastLiftId)
        assertEquals(50L, dataSource.lastLimit)
        assertEquals(Sorting.date, dataSource.lastSorting)
    }

    @Test
    fun `listen delegates to data source`() = runTest {
        val set = LBSet(id = "1", movementId = "v1", weight = 100.0, reps = 5)
        val dataSource = fakeSetDataSource(sets = listOf(set))
        val repository = SetRepository(dataSource)

        val result = repository.listen("1").first()

        assertEquals(set, result)
        assertEquals("1", dataSource.lastId)
    }

    @Test
    fun `save delegates to data source`() = runTest {
        val set = LBSet(id = "1", movementId = "v1", weight = 100.0, reps = 5)
        val dataSource = fakeSetDataSource()
        val repository = SetRepository(dataSource)

        repository.save(set)

        assertEquals(set, dataSource.savedSet)
    }

    @Test
    fun `delete delegates to data source`() = runTest {
        val set = LBSet(id = "1", movementId = "v1", weight = 100.0, reps = 5)
        val dataSource = fakeSetDataSource()
        val repository = SetRepository(dataSource)

        repository.delete(set)

        assertEquals(set, dataSource.deletedSet)
    }

    @Test
    fun `deleteAll delegates to data source`() = runTest {
        val dataSource = fakeSetDataSource()
        val repository = SetRepository(dataSource)

        repository.deleteAll()

        assertEquals(true, dataSource.deleteAllCalled)
    }

    @Test
    fun `deleteAll with variationId delegates to data source`() = runTest {
        val dataSource = fakeSetDataSource()
        val repository = SetRepository(dataSource)

        repository.deleteAll("v1")

        assertEquals("v1", dataSource.deletedVariationId)
    }

    @Test
    fun `listenAll uses default parameters`() = runTest {
        val dataSource = fakeSetDataSource()
        val repository = SetRepository(dataSource)

        repository.listenAll()

        assertEquals(null, dataSource.lastStartDate)
        assertEquals(null, dataSource.lastEndDate)
        assertEquals(null, dataSource.lastVariationId)
        assertEquals(Long.MAX_VALUE, dataSource.lastLimit)
        assertEquals(null, dataSource.lastReps)
        assertEquals(Sorting.date, dataSource.lastSorting)
        assertEquals(Order.Descending, dataSource.lastOrder)
    }
}
