package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.domain.models.LBSet
import com.lift.bro.testdoubles.FakeSetRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTotalRepsUseCaseTest {

    @Test
    fun `Given sets with various reps When invoked Then returns sum of all reps`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", movementId = "v1", reps = 10),
            LBSet(id = "2", movementId = "v1", reps = 8),
            LBSet(id = "3", movementId = "v2", reps = 12),
            LBSet(id = "4", movementId = "v2", reps = 5)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalRepsUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(35L, result)
    }

    @Test
    fun `Given empty sets When invoked Then returns 0`() = runTest {
        // Given
        val repository = FakeSetRepository(emptyList())
        val useCase = GetTotalRepsUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0L, result)
    }

    @Test
    fun `Given date range When invoked Then only includes sets in range`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        val sets = listOf(
            LBSet(id = "1", movementId = "v1", reps = 10),
            LBSet(id = "2", movementId = "v1", reps = 8),
            LBSet(id = "3", movementId = "v2", reps = 12)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalRepsUseCase(repository)

        // When
        val result = useCase(startDate = startDate, endDate = endDate).first()

        // Then
        assertEquals(30L, result)
        assertEquals(startDate, repository.lastStartDate)
        assertEquals(endDate, repository.lastEndDate)
    }

    @Test
    fun `Given sets with 1 rep each When invoked Then returns count of sets`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", movementId = "v1", reps = 1),
            LBSet(id = "2", movementId = "v1", reps = 1),
            LBSet(id = "3", movementId = "v1", reps = 1)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalRepsUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(3L, result)
    }

    @Test
    fun `Given single set When invoked Then returns that set's reps`() = runTest {
        // Given
        val sets = listOf(
            LBSet(id = "1", movementId = "v1", reps = 15)
        )
        val repository = FakeSetRepository(sets)
        val useCase = GetTotalRepsUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(15L, result)
    }
}
