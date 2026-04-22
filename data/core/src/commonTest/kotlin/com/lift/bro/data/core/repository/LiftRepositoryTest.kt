package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiftRepositoryTest {

    @Test
    fun `listenAll delegates to data source`() = runTest {
        val lifts = listOf(
            Category(id = "1", name = "Bench Press"),
            Category(id = "2", name = "Squat")
        )
        val dataSource = FakeLiftDataSource(lifts = lifts)
        val repository = LiftRepository(dataSource)

        val result = repository.listenAll().first()

        assertEquals(lifts, result)
    }

    @Test
    fun `getAll delegates to data source`() = runTest {
        val lifts = listOf(
            Category(id = "1", name = "Deadlift"),
            Category(id = "2", name = "Press")
        )
        val dataSource = FakeLiftDataSource(lifts = lifts)
        val repository = LiftRepository(dataSource)

        val result = repository.getAll()

        assertEquals(lifts, result)
    }

    @Test
    fun `get delegates to data source`() = runTest {
        val lift = Category(id = "1", name = "Row")
        val dataSource = FakeLiftDataSource(singleLift = lift)
        val repository = LiftRepository(dataSource)

        val result = repository.get("1").first()

        assertEquals(lift, result)
    }

    @Test
    fun `save delegates to data source`() = runTest {
        val lift = Category(id = "1", name = "Clean")
        val dataSource = FakeLiftDataSource()
        val repository = LiftRepository(dataSource)

        val result = repository.save(lift)

        assertTrue(result)
        assertEquals(lift, dataSource.savedLift)
    }

    @Test
    fun `deleteAll delegates to data source`() = runTest {
        val dataSource = FakeLiftDataSource()
        val repository = LiftRepository(dataSource)

        repository.deleteAll()

        assertTrue(dataSource.deleteAllCalled)
    }

    @Test
    fun `delete delegates to data source`() = runTest {
        val dataSource = FakeLiftDataSource()
        val repository = LiftRepository(dataSource)

        repository.delete("test-id")

        assertEquals("test-id", dataSource.deletedId)
    }

    // Fake data source for testing
    private class FakeLiftDataSource(
        private val lifts: List<Category> = emptyList(),
        private val singleLift: Category? = null
    ) : LiftDataSource {
        var savedLift: Category? = null
        var deleteAllCalled = false
        var deletedId: String? = null

        override fun listenAll(): Flow<List<Category>> = flowOf(lifts)

        override fun getAll(): List<Category> = lifts

        override fun get(id: String?): Flow<Category?> = flowOf(singleLift)

        override fun save(lift: Category): Boolean {
            savedLift = lift
            return true
        }

        override suspend fun deleteAll() {
            deleteAllCalled = true
        }

        override suspend fun delete(id: String) {
            deletedId = id
        }
    }
}
