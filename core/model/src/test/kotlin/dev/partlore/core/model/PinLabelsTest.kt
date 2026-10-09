package dev.partlore.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PinLabelsTest {
    private fun pin(
        id: String,
        chip: String? = null,
        module: String? = null,
        board: String? = null,
        arduino: String? = null,
    ) = PinInfo(
        id, "h", 1, 1, chip, module, board, arduino, emptyList(), PinDirection.Io, null, null, emptyList(),
        PinSafety.Ok, null, null, emptyList(), emptyList(),
    )

    private fun pinout(vararg pins: PinInfo) =
        Pinout("t/p", "P", emptyList(), pins.toList(), emptyList(), null, "0.1.0")

    @Test
    fun aPinShowsTheNameOfTheChosenMode() {
        val gpio2 = pin("io2", chip = "GPIO2", module = "IO2", board = "D4")
        assertEquals(PinLabel("D4", fallback = false), gpio2.label(PinLabelMode.Board))
        assertEquals(PinLabel("GPIO2", fallback = false), gpio2.label(PinLabelMode.Chip))
    }

    @Test
    fun aMissingNameFallsBackBoardModuleChipThenId() {
        assertEquals(
            PinLabel("IO2", fallback = true),
            pin("io2", chip = "GPIO2", module = "IO2").label(PinLabelMode.Arduino),
        )
        assertEquals(PinLabel("gnd", fallback = true), pin("gnd").label(PinLabelMode.Board))
    }

    @Test
    fun onlyModesSomePinHasAreOffered() {
        val uno = pinout(pin("d0", chip = "PD0", arduino = "0"), pin("a0", chip = "PC0", arduino = "A0"))
        assertEquals(listOf(PinLabelMode.Chip, PinLabelMode.Arduino), uno.labelModes())
        assertEquals(PinLabelMode.Arduino, uno.labelMode(preferred = PinLabelMode.Board))
        assertEquals(PinLabelMode.Chip, uno.labelMode(preferred = PinLabelMode.Chip))
    }
}
