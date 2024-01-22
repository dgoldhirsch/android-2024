package com.cornmuffin.prototype

import com.cornmuffin.prototype.util.roundToNearestHalf
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOLERANCE = 0.00000001

private val ks = listOf(0.0, 1.0, 2.0)

class DoubleKtxTest {

    @Test
    fun `Double roundToNearestHalf - given zero is zero`() {
        assertEquals(0.0, 0.1.roundToNearestHalf())
    }

    @Test
    fun `Double roundToNearestHalf - given a value between K and K plus one quarter - rounds down to K`() {
        ks.forEach {
            assertEquals(it, it.roundToNearestHalf(), TOLERANCE)
            assertEquals(it, (it + 0.24).roundToNearestHalf(), TOLERANCE)
        }
    }

    @Test
    fun `Double roundToNearestHalf - given a value between K plus a quarter and K plus a half - rounds up to K plus a half`() {
        ks.forEach {
            assertEquals(it + 0.5, (it + 0.25).roundToNearestHalf(), TOLERANCE)
            assertEquals(it + 0.5, (it + 0.49).roundToNearestHalf(), TOLERANCE)
        }
    }

    @Test
    fun `Double roundToNearestHalf - given a value between K plus a half and K plus three quarters - rounds down to K and a half`() {
        ks.forEach {
            assertEquals(it + 0.5, (it + 0.50).roundToNearestHalf(), TOLERANCE)
            assertEquals(it + 0.5, (it + 0.74).roundToNearestHalf(), TOLERANCE)
        }
    }

    @Test
    fun `Double roundToNearestHalf - given a value between K plus three quarters and K plus one - rounds up to K plus`() {
        ks.forEach {
            assertEquals(it + 1, (it + 0.75).roundToNearestHalf(), TOLERANCE)
            assertEquals(it + 1, (it + 0.99).roundToNearestHalf(), TOLERANCE)
        }
    }
}
