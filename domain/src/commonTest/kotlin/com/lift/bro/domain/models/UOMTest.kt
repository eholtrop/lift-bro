package com.lift.bro.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class UOMTest {

    @Test
    fun `Given same unit When converting Then returns same value`() {
        assertEquals(100.0, UOM.KG.convert(100.0, UOM.KG))
        assertEquals(100.0, UOM.POUNDS.convert(100.0, UOM.POUNDS))
    }

    @Test
    fun `Given KG When converting to pounds Then multiplies by KG_TO_LBS`() {
        val result = UOM.KG.convert(100.0, UOM.POUNDS)

        // 100 * 2.2046226218 = 220.46226218
        assertEquals(220.46226218, result, 0.00001)
    }

    @Test
    fun `Given pounds When converting to KG Then multiplies by LBS_TO_KG`() {
        val result = UOM.POUNDS.convert(100.0, UOM.KG)

        // 100 * 0.45356237 = 45.356237
        assertEquals(45.356237, result, 0.00001)
    }

    @Test
    fun `Given 1 KG When converting to pounds Then returns 2 point 205`() {
        val result = UOM.KG.convert(1.0, UOM.POUNDS)

        assertEquals(2.2046226218, result, 0.00001)
    }

    @Test
    fun `Given 1 pound When converting to KG Then returns 0 point 454`() {
        val result = UOM.POUNDS.convert(1.0, UOM.KG)

        assertEquals(0.45356237, result, 0.00001)
    }

    @Test
    fun `Given zero value When converting Then returns zero`() {
        assertEquals(0.0, UOM.KG.convert(0.0, UOM.POUNDS))
        assertEquals(0.0, UOM.POUNDS.convert(0.0, UOM.KG))
    }

    @Test
    fun `Given large weight When converting Then returns correct value`() {
        // 200 KG = 440.92 lbs
        val result = UOM.KG.convert(200.0, UOM.POUNDS)

        assertEquals(440.92452436, result, 0.001)
    }
}
