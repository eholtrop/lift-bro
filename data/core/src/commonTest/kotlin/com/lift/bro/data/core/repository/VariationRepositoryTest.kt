package com.lift.bro.data.core.repository

import com.lift.bro.data.core.testdoubles.fakeVariationDataSource
import com.lift.bro.domain.models.Movement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VariationRepositoryTest {

    @Test
    fun `listenAll delegates to data source`() = runTest {
        val variations = listOf(
            Movement(id = "v1", name = "Bench Press"),
            Movement(id = "v2", name = "Squat")
        )
        val dataSource = fakeVariationDataSource(variations = variations)
        val repository = VariationRepository(dataSource)

        val result = repository.listenAll().first()

        assertEquals(variations, result)
    }

    @Test
    fun `listenAll with liftId delegates to data source`() = runTest {
        val variations = listOf(
            Movement(id = "v1", name = "Bench Press")
        )
        val dataSource = fakeVariationDataSource(variations = variations)
        val repository = VariationRepository(dataSource)

        val result = repository.listenAll(liftId = "lift1").first()

        assertEquals(variations, result)
        assertEquals("lift1", dataSource.lastLiftId)
    }

    @Test
    fun `listen delegates to data source`() = runTest {
        val variation = Movement(id = "v1", name = "Bench Press")
        val dataSource = fakeVariationDataSource(singleVariation = variation)
        val repository = VariationRepository(dataSource)

        val result = repository.listen("v1").first()

        assertEquals(variation, result)
        assertEquals("v1", dataSource.lastId)
    }

    @Test
    fun `get delegates to data source`() {
        val variation = Movement(id = "v1", name = "Bench Press")
        val dataSource = fakeVariationDataSource(singleVariation = variation)
        val repository = VariationRepository(dataSource)

        val result = repository.get("v1")

        assertEquals(variation, result)
        assertEquals("v1", dataSource.lastId)
    }

    @Test
    fun `get with null variationId returns null`() {
        val dataSource = fakeVariationDataSource()
        val repository = VariationRepository(dataSource)

        val result = repository.get(null)

        assertEquals(null, result)
        assertEquals("", dataSource.lastId)
    }

    @Test
    fun `getAll delegates to data source`() {
        val variations = listOf(
            Movement(id = "v1", name = "Bench Press"),
            Movement(id = "v2", name = "Squat")
        )
        val dataSource = fakeVariationDataSource(variations = variations)
        val repository = VariationRepository(dataSource)

        val result = repository.getAll()

        assertEquals(variations, result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `save delegates to data source`() = runTest {
        val variation = Movement(id = "v1", name = "Bench Press")
        val dataSource = fakeVariationDataSource()
        val repository = VariationRepository(dataSource)

        repository.save(variation)
        advanceUntilIdle()

        assertEquals(variation, dataSource.savedVariation)
    }

    @Test
    fun `deleteAll delegates to data source`() = runTest {
        val dataSource = fakeVariationDataSource()
        val repository = VariationRepository(dataSource)

        repository.deleteAll()

        assertEquals(true, dataSource.deleteAllCalled)
    }
}
