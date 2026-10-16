package dev.partlore.core.content

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.sqlite.execSQL
import dev.partlore.core.model.BoardEdge
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.Side
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PinoutReaderTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun aPinoutComesThroughWithItsRuns() {
        val pinout = openPack(buildPack(tmp) { addBoard() }).use { checkNotNull(it.pinout(BOARD)) }
        val header = pinout.headers.single()
        assertEquals("j1", header.id)
        assertEquals(BoardEdge.Left, header.runs.single().edge)
        assertEquals(Side.Top, header.runs.single().first)
        assertEquals(listOf("gpio1", "gpio0", "gnd"), pinout.pins.sortedBy { it.index }.map { it.id })
        val gpio0 = pinout.pins.single { it.id == "gpio0" }
        assertEquals(PinSafety.Caution, gpio0.safe)
        assertEquals("high", gpio0.strapping?.mustBe)
        assertEquals(PinDirection.Ground, pinout.pins.single { it.id == "gnd" }.direction)
        assertEquals("Test Board", pinout.partName)
    }

    @Test
    fun thePartPageCarriesItsPinout() {
        val page = openPack(buildPack(tmp) { addBoard() }).use { checkNotNull(it.part(BOARD)) }
        assertEquals(3, page.pinout?.pins?.size)
    }

    @Test
    fun anUnknownLayoutValueLeavesTheHeaderUnplaced() {
        val pack = buildPack(tmp) { addBoard() }
        BundledSQLiteDriver().open(pack.path, SQLITE_OPEN_READWRITE).use {
            it.execSQL("UPDATE header_edge SET first = 'diagonal'")
        }
        val pinout = openPack(pack).use { checkNotNull(it.pinout(BOARD)) }
        assertEquals(emptyList<Any>(), pinout.headers.single().runs)
        assertEquals(3, pinout.pins.size)
    }

    @Test
    fun aPartWithoutPinsHasNoPinout() {
        val pack = buildPack(tmp) {
            addBoard()
            delete("parts/$BOARD/pins.yaml")
            delete("parts/$BOARD/gotchas.yaml")
        }
        openPack(pack).use { assertNull(it.pinout(BOARD)) }
    }
}
