package dev.partlore.core.content

import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.PinoutLayout
import dev.partlore.core.model.PlacedBoard
import dev.partlore.core.model.Rotation
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.packer.Packer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// Real content carries real checked_on dates, so this packs with the real date, not the test TODAY.
// Pin 1 of every real header sits where the cited figure shows it (see the plan's verified layout table).
class RealPinoutTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun pinout(id: String): Pinout {
        val content = File(System.getProperty("partlore.contentDir"))
        val result = Packer(content, tmp.newFolder()).pack(Mode.PREVIEW)
        check(result is Packer.Result.Packed) { result.diagnostics.take(5).joinToString("\n") { it.plain("") } }
        return openPack(result.file).use { checkNotNull(it.pinout(id)) }
    }

    private fun PlacedBoard.cellOf(pin: PinInfo): Pair<Int, Int> = pins.single {
        it.pinId == pin.id
    }.let { it.x to it.y }

    @Test
    fun devKitCHas3v3TopLeftAndGndTopRight() {
        val p = pinout("espressif/esp32-devkitc-v4-wroom-32e")
        val board = PinoutLayout.place(p, Rotation.R0)
        assertEquals(0 to 1, board.cellOf(p.pins.single { it.headerId == "j2" && it.index == 1 }))
        assertEquals("3V3", p.pins.single { it.headerId == "j2" && it.index == 1 }.boardLabel)
        assertEquals(board.columns - 1 to 1, board.cellOf(p.pins.single { it.headerId == "j3" && it.index == 1 }))
        assertEquals("GND", p.pins.single { it.headerId == "j3" && it.index == 1 }.boardLabel)
        assertEquals(emptyList<String>(), board.unplacedHeaders)
        assertEquals(38, board.pins.size)
    }

    @Test
    fun wroomPad25IsAtTheBottomOfTheRightEdge() {
        val p = pinout("espressif/esp32-wroom-32e")
        val board = PinoutLayout.place(p, Rotation.R0)
        assertEquals(board.columns - 1 to 14, board.cellOf(p.pins.single { it.index == 25 }))
        assertEquals(38, board.pins.size)
    }

    @Test
    fun unoD0IsTheRightmostTopPinAndA0TheLeftmostAnalogPin() {
        val p = pinout("arduino/uno-r3")
        val board = PinoutLayout.place(p, Rotation.R0)
        val top = board.pins.filter { it.y == 0 }
        val d0 = p.pins.single { it.headerId == "digital-low" && it.index == 1 }
        assertEquals(top.maxOf { it.x }, board.cellOf(d0).first)
        val a0 = p.pins.single { it.headerId == "analog" && it.index == 1 }
        val analog = p.pins.filter { it.headerId == "analog" }.map { board.cellOf(it).first }
        assertEquals(analog.min(), board.cellOf(a0).first)
        assertEquals(38, board.pins.size)
    }

    // Every run's pin 1, so a flipped `first` or `row1` on any real run fails here.
    @Test
    fun wroomPads1And15And38StartTheirRuns() {
        val p = pinout("espressif/esp32-wroom-32e")
        val board = PinoutLayout.place(p, Rotation.R0)
        assertEquals(0 to 1, board.cellOf(p.pins.single { it.index == 1 }))
        assertEquals(1 to board.rows - 1, board.cellOf(p.pins.single { it.index == 15 }))
        assertEquals(board.columns - 1 to 1, board.cellOf(p.pins.single { it.index == 38 }))
    }

    @Test
    fun unoHeadersStartWhereTheBoardShowsThem() {
        val p = pinout("arduino/uno-r3")
        val board = PinoutLayout.place(p, Rotation.R0)
        fun xs(header: String) = p.pins.filter { it.headerId == header }.map { board.cellOf(it).first }
        fun pin1(header: String, row: Int = 1) =
            board.cellOf(p.pins.single { it.headerId == header && it.index == 1 && it.row == row })
        // IOH starts with D8 at its right end and sits left of IOL on the top edge.
        assertEquals(xs("digital-high").max() to 0, pin1("digital-high"))
        assertTrue(xs("digital-high").max() < xs("digital-low").min())
        // POWER starts with NC at its left end and sits left of AD on the bottom edge.
        assertEquals(xs("power").min() to board.rows - 1, pin1("power"))
        assertTrue(xs("power").max() < xs("analog").min())
        // ICSP: pin 1 (CIPO) at the top of the left line, row 2 beside it on the right.
        val icsp = p.pins.filter { it.headerId == "icsp" }.map { board.cellOf(it) }
        val cipo = pin1("icsp")
        assertEquals(icsp.minOf { it.first } to icsp.minOf { it.second }, cipo)
        assertEquals(cipo.first + 1 to cipo.second, pin1("icsp", row = 2))
    }
}
