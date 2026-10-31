package dev.partlore.tools.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalsTest {
    @Test
    fun aGpioCellGivesTheChipNameAndItsFunctionsGpioFirst() {
        assertEquals(
            PinFacts(
                "GPIO4",
                "io",
                listOf(Fn("gpio", null), Fn("rtc", "RTC_GPIO4"), Fn("touch", "TOUCH4"), Fn("adc", "ADC1_CH3")),
                emptyList(),
            ),
            Signals.read("I/O/T", "RTC_GPIO4, GPIO4, TOUCH4, ADC1_CH3"),
        )
        assertEquals(
            listOf(Fn("gpio", null), Fn("adc", "ADC2_CH8"), Fn("dac", "DAC_1")),
            Signals.read("I/O", "GPIO25, ADC2_CH8, DAC_1").functions,
        )
    }

    @Test
    fun uartJtagClockUsbAndSpiSignalsAreTyped() {
        assertEquals(
            listOf(
                Fn("gpio", null),
                Fn("rtc", "RTC_GPIO20"),
                Fn("uart", "U1CTS"),
                Fn("adc", "ADC2_CH9"),
                Fn("clock", "CLK_OUT1"),
                Fn("usb", "USB_D+"),
            ),
            Signals.read("I/O/T", "RTC_GPIO20, GPIO20, U1CTS, ADC2_CH9, CLK_OUT1, USB_D+").functions,
        )
        assertEquals(listOf(Fn("gpio", null), Fn("jtag", "MTMS")), Signals.read("I/O/T", "MTMS, GPIO42").functions)
        assertEquals(
            listOf(Fn("gpio", null), Fn("spi", "SPICLK_P"), Fn("spi", "SUBSPICLK_P_DIFF")),
            Signals.read("I/O/T", "GPIO47, SPICLK_P, SUBSPICLK_P_DIFF").functions,
        )
        assertEquals(
            listOf(Fn("gpio", null), Fn("uart", "U0RTS"), Fn("clock", "XTAL_32K_P")),
            Signals.read("I/O/T", "GPIO15, U0RTS, XTAL_32K_P").functions,
        )
    }

    @Test
    fun powerAndGroundRowsHaveNoFunctions() {
        assertEquals(PinFacts(null, "power", emptyList(), emptyList()), Signals.read("P", "3.3 V power supply"))
        assertEquals(PinFacts(null, "ground", emptyList(), emptyList()), Signals.read("G", "Ground"))
    }

    @Test
    fun resetAndBootFollowTheCheckedFiles() {
        assertEquals(
            PinFacts(null, "in", listOf(Fn("reset", "CHIP_PU")), emptyList()),
            Signals.read("I", "CHIP_PU, Reset"),
        )
        assertEquals(listOf(Fn("reset", "EN")), Signals.read("I", "EN").functions)
        assertEquals(
            listOf(Fn("gpio", null), Fn("adc", "ADC2_CH1"), Fn("touch", "TOUCH_CH1"), Fn("boot", null)),
            Signals.read("I/O", "GPIO0, ADC2_CH1, TOUCH_CH1, Boot").functions,
        )
    }

    @Test
    fun modulePadNamesAndVdetAreLeftOut() {
        assertEquals(
            PinFacts("GPIO36", "in", listOf(Fn("gpio", null), Fn("adc", "ADC1_CH0")), emptyList()),
            Signals.read("I", "GPIO36, ADC1_CH0, S_VP"),
        )
        assertEquals(
            listOf(Fn("gpio", null), Fn("adc", "ADC1_CH6")),
            Signals.read("I", "GPIO34, ADC1_CH6, VDET_1").functions,
        )
    }

    @Test
    fun boardThingsAndFlashNamesAreFlaggedNotTyped() {
        val led = Signals.read("I/O/T", "GPIO38, FSPIWP, SUBSPIWP, RGB LED")
        assertEquals(listOf(Fn("gpio", null), Fn("spi", "FSPIWP"), Fn("spi", "SUBSPIWP")), led.functions)
        assertEquals(listOf("RGB LED"), led.flags)
        assertEquals(listOf("CLK"), Signals.read("I/O", "GPIO6, CLK").flags)
    }

    @Test
    fun anUnknownSignalStopsTheImportEvenNextToKnownOnes() {
        val error = assertThrows(ImportException::class.java) { Signals.read("I/O", "GPIO5, ADC1_CH4, FOO_BAR") }
        assertTrue(error.message, error.message!!.contains("'FOO_BAR'"))
    }

    @Test
    fun anUnknownTypeStopsTheImport() {
        val error = assertThrows(ImportException::class.java) { Signals.read("X", "GPIO5") }
        assertTrue(error.message, error.message!!.contains("'X'"))
    }

    @Test
    fun powerWordsOnASignalRowAreNotAccepted() {
        assertThrows(ImportException::class.java) { Signals.read("I/O", "GPIO5, Ground") }
    }
}
