package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTotalWeightMovedUseCaseTest {

    @Test
    fun `Given sets with weight and reps When invoked Then returns sum of weight times reps`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", variationId = "v1", weight = 100.0, reps = 10), // 1000
            LBSet(id = "2", variationId = "v1", weight = 50.0, reps = 8), // 400
            LBSet(id = "3", variationId = "v2", weight = 200.0, reps = 5), // 1000
            LBSet(id = "4", variationId = "v2", weight = 75.0, reps = 12) // 900
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase().first()

        // Then - Total: 1000 + 400 + 1000 + 900 = 3300
        assertEquals(3300.0, result)
    }

    @Test
    fun `Given empty sets When invoked Then returns 0`() = runTest {
        // Given
        val repository = FakeSetRepository(emptyList())
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0.0, result)
    }

    @Test
    fun `Given date range When invoked Then only includes sets in range`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        val sets = listOf(
            LBSet(id = "1", variationId = "v1", weight = 100.0, reps = 10),
            LBSet(id = "2", variationId = "v1", weight = 50.0, reps = 8)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase(startDate = startDate, endDate = endDate).first()

        // Then - Total: 1000 + 400 = 1400
        assertEquals(1400.0, result)
        assertEquals(startDate, repository.lastStartDate)
        assertEquals(endDate, repository.lastEndDate)
    }

    @Test
    fun `Given mixed weights When invoked Then calculates correctly`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", variationId = "v1", weight = 45.5, reps = 5), // 227.5
            LBSet(id = "2", variationId = "v1", weight = 102.3, reps = 3), // 306.9
            LBSet(id = "3", variationId = "v2", weight = 67.8, reps = 10) // 678.0
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase().first()

        // Then - Total: 227.5 + 306.9 + 678.0 = 1212.4
        assertEquals(1212.4, result, 0.01)
    }

    @Test
    fun `Given sets with zero weight When invoked Then returns 0`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", variationId = "v1", weight = 0.0, reps = 10),
            LBSet(id = "2", variationId = "v1", weight = 0.0, reps = 5)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0.0, result)
    }

    @Test
    fun `Given single set When invoked Then returns that set's total weight moved`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", variationId = "v1", weight = 225.0, reps = 5)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase().first()

        // Then - 225 * 5 = 1125
        assertEquals(1125.0, result)
    }

    @Test
    fun `Given sets with 1 rep each When invoked Then returns sum of weights`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", variationId = "v1", weight = 100.0, reps = 1),
            LBSet(id = "2", variationId = "v1", weight = 150.0, reps = 1),
            LBSet(id = "3", variationId = "v1", weight = 200.0, reps = 1)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalWeightMovedUseCase(repository)

        // When
        val result = useCase().first()

        // Then - 100 + 150 + 200 = 450
        assertEquals(450.0, result)
    }

    // Fake repository for testing
    private class FakeSetRepository(
        private val sets: List<LBSet>
    ) : ISetRepository {
        var lastStartDate: LocalDate? = null
        var lastEndDate: LocalDate? = null

        override fun listenAll(
            startDate: LocalDate?,
            endDate: LocalDate?,
            variationId: String?,
            reps: Long?,
            limit: Long,
            sorting: Sorting,
            order: Order
        ): Flow<List<LBSet>> {
            lastStartDate = startDate
            lastEndDate = endDate
            return flowOf(sets)
        }

        override fun listenAllForLift(
            liftId: String,
            startDate: LocalDate?,
            endDate: LocalDate?,
            limit: Long,
            sorting: Sorting
        ): Flow<List<LBSet>> = flowOf(sets)

        override fun listen(id: String): Flow<LBSet?> = flowOf(null)
        override suspend fun save(lbSet: LBSet) {}
        override suspend fun delete(lbSet: LBSet) {}
        override suspend fun deleteAll() {}
        override suspend fun deleteAll(variationId: String) {}
    }
}
