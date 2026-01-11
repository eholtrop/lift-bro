package com.lift.bro.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class VariationTest {

    @Test
    fun `Given variation with name and lift When fullName Then returns combined trimmed name`() {
        val variation = Variation(
            name = "Close Grip",
            lift = Lift(name = "Bench Press")
        )

        assertEquals("Close Grip Bench Press", variation.fullName)
    }

    @Test
    fun `Given variation with null name When fullName Then returns only lift name`() {
        val variation = Variation(
            name = null,
            lift = Lift(name = "Squat")
        )

        assertEquals("Squat", variation.fullName)
    }

    @Test
    fun `Given variation with null lift When fullName Then returns variation name with null appended`() {
        val variation = Variation(
            name = "Bulgarian Split Squat",
            lift = null
        )

        // When lift is null, the template produces "name null" which is then trimmed
        assertEquals("Bulgarian Split Squat null", variation.fullName)
    }

    @Test
    fun `Given variation with whitespace When fullName Then trims correctly`() {
        val variation = Variation(
            name = "  Narrow  ",
            lift = Lift(name = "  Pull Up  ")
        )

        assertEquals("Narrow Pull Up", variation.fullName)
    }

    @Test
    fun `Given variation with both null When fullName Then returns null`() {
        val variation = Variation(
            name = null,
            lift = null
        )

        // When both are null, template produces " null" which trims to "null"
        assertEquals("null", variation.fullName)
    }

    @Test
    fun `Given variation with empty name When fullName Then returns only lift name`() {
        val variation = Variation(
            name = "",
            lift = Lift(name = "Deadlift")
        )

        assertEquals("Deadlift", variation.fullName)
    }

    @Test
    fun `Given variation with empty lift name When fullName Then returns only variation name`() {
        val variation = Variation(
            name = "Sumo",
            lift = Lift(name = "")
        )

        assertEquals("Sumo", variation.fullName)
    }

    @Test
    fun `Given variation with name containing only spaces When fullName Then returns lift name`() {
        val variation = Variation(
            name = "   ",
            lift = Lift(name = "Press")
        )

        assertEquals("Press", variation.fullName)
    }
}
