package dev.partlore.feature.pinout

import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Test

class PinFilterTest {
    private val pinout = SampleContent.devBoardPinout

    @Test
    fun onlyFiltersWithMatchingPinsAreOffered() {
        assertEquals(
            listOf(PinFilter.I2c, PinFilter.Adc, PinFilter.Power, PinFilter.Strapping, PinFilter.InputOnly),
            pinout.filters(),
        )
    }

    @Test
    fun powerCoversPowerAndGround() {
        assertEquals(listOf("3v3", "gnd-left", "gnd-right"), pinout.pins.filter(PinFilter.Power::matches).map { it.id })
    }

    @Test
    fun i2cMatchesByFunction() {
        assertEquals(listOf("io21", "io22"), pinout.pins.filter(PinFilter.I2c::matches).map { it.id })
    }
}
