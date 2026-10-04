package dev.partlore.core.content

import dev.partlore.core.model.Cite
import dev.partlore.core.model.FiveVoltTolerance
import dev.partlore.core.model.Glance
import dev.partlore.core.model.GotchaItem
import dev.partlore.core.model.I2cRow
import dev.partlore.core.model.PartRef
import dev.partlore.core.model.Severity
import dev.partlore.core.model.VerificationLevel
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PackReaderPartTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun page(setup: dev.partlore.tools.content.testing.ContentFixture.() -> Unit = { addBoard() }) =
        openPack(buildPack(tmp, setup = setup)).use { checkNotNull(it.part(BOARD)) }

    @Test
    fun theHeadAndAliasesComeThrough() {
        val part = page()
        assertEquals("Test Board", part.name)
        assertEquals(listOf("TB-1"), part.aliases)
        assertEquals("board", part.kind)
        assertEquals("testmaker", part.manufacturer)
        assertEquals("A board that exists only in tests.", part.summary)
        assertEquals("0.0.1", part.packVersion)
    }

    @Test
    fun specsKeepTheirOrderLabelsAndCitations() {
        val part = page()
        assertEquals(listOf("Supply voltage", "Logic level", "Wi-Fi"), part.specs.map { it.label })
        val supply = part.specs[0]
        assertEquals(listOf(3.0, 3.3, 3.6), listOf(supply.min, supply.typ, supply.max))
        assertEquals("V", supply.unit)
        assertEquals(Cite("s1", 2, null, null), supply.cite)
        assertEquals(3.3, part.specs[1].number)
        assertEquals("802.11 b/g/n", part.specs[2].text)
        assertEquals(Cite("s2", null, "Radio", null), part.specs[2].cite)
        assertEquals(3.9, part.absoluteMax.single().max)
    }

    @Test
    fun i2cGlanceAndGotchasComeThrough() {
        val part = page()
        assertEquals(listOf(I2cRow(0x3c, true, "ADDR to GND", Cite("s1", 4, null, null))), part.i2c)
        assertEquals(Glance(3.3, FiveVoltTolerance.None, 2, 1, "802.11 b/g/n", null), part.glance)
        assertEquals(
            listOf(
                GotchaItem(
                    "boot-pin",
                    Severity.Caution,
                    "GPIO0 must be high at reset",
                    "Pulling it low at reset starts the bootloader.",
                    listOf("D0"),
                    listOf(Cite("s1", 6, null, null)),
                ),
            ),
            part.gotchas,
        )
    }

    @Test
    fun sourcesAndStatusesComeThrough() {
        val part = page()
        assertEquals(listOf("s1", "s2"), part.sources.map { it.id })
        assertEquals("link-only", part.sources[0].license)
        assertEquals(VerificationLevel.Checked, part.partStatus.level)
        assertEquals(listOf("tester"), part.partStatus.checkedBy)
        assertEquals(listOf("s1", "s2"), part.partStatus.against)
        assertEquals(listOf("s1"), checkNotNull(part.pinsStatus).against)
        assertEquals("2026-01-01", checkNotNull(part.gotchasStatus).checkedOn)
    }

    @Test
    fun usesUsedByAndRelatedLinkBothWays() {
        val module = "testmaker/radio-module"
        val board = page {
            addBoard()
            addBoard(module)
            edit("parts/$module/part.yaml") { it.replace("name: Test Board", "name: Radio Module") }
            edit("$BOARD_DIR/part.yaml") { it.replace("summary:", "uses: $module\nrelated: [$module]\nsummary:") }
        }
        assertEquals(PartRef(module, "Radio Module"), board.uses)
        assertEquals(listOf(PartRef(module, "Radio Module")), board.related)
        val usedBy = openPack(
            buildPack(tmp) {
                addBoard()
                addBoard(module)
                edit("$BOARD_DIR/part.yaml") { it.replace("summary:", "uses: $module\nsummary:") }
            },
        ).use { checkNotNull(it.part(module)).usedBy }
        assertEquals(listOf(PartRef(BOARD, "Test Board")), usedBy)
    }

    @Test
    fun unicodeSurvives() {
        val part = page {
            addBoard()
            edit("$BOARD_DIR/part.yaml") { it.replace("name: Test Board", "name: Thermo µΩ Board 25 °C") }
        }
        assertEquals("Thermo µΩ Board 25 °C", part.name)
    }

    @Test
    fun anUnknownPartIsNull() {
        openPack(buildPack(tmp) { addBoard() }).use { assertNull(it.part("testmaker/nothing")) }
    }
}
