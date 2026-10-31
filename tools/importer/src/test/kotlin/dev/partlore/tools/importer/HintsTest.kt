package dev.partlore.tools.importer

import dev.partlore.tools.importer.rst.PinTable
import dev.partlore.tools.importer.rst.TableRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HintsTest {
    private val tables =
        listOf(
            PinTable(
                "J1",
                listOf(
                    TableRow(1, "3V3", "P", "3.3 V power supply", emptySet()),
                    TableRow(2, "IO4", "I/O/T", "GPIO4, ADC1_CH3", emptySet()),
                    TableRow(3, "G", "G", "Ground", emptySet()),
                ),
            ),
        )

    @Test
    fun aHintsFileIsReadAndMatchesTheTables() {
        val hints = Hints.read(HINTS)
        hints.check(tables)
        assertEquals("example/board", hints.part.get("id").asString())
        assertEquals(
            Hints.GuideHint("docs/en/board/user_guide.rst", "https://example.com/board.html", "v1.0"),
            hints.guide,
        )
        assertEquals(listOf("J1"), hints.headers.map { it.table })
        assertEquals(listOf(1, 2, 3), hints.pins.map { it.no })
        assertEquals("4", hints.pin("J1", 2).get("board_label").asString())
    }

    @Test
    fun aMistypedKeyStops() {
        val error = assertThrows(ImportException::class.java) { Hints.read(HINTS.replace("safe: ok", "saf: ok")) }
        assertTrue(error.message, error.message!!.contains("'saf'"))
    }

    @Test
    fun aKeyWrittenTwiceStops() {
        assertThrows(ImportException::class.java) {
            Hints.read(HINTS.replace("{ table: J1, no: 3, safe: ok }", "{ table: J1, no: 3, safe: ok, safe: avoid }"))
        }
    }

    @Test
    fun aPinWithoutSafeStops() {
        val error =
            assertThrows(ImportException::class.java) {
                Hints.read(HINTS.replace("  - { table: J1, no: 3, safe: ok }\n", "")).check(tables)
            }
        assertTrue(error.message, error.message!!.contains("J1 No. 3"))
    }

    @Test
    fun aJudgementWithoutACiteStops() {
        val error =
            assertThrows(ImportException::class.java) {
                Hints.read(HINTS.replace("safe: caution, cites: [ { source: s3, page: 2 } ]", "safe: caution"))
            }
        assertTrue(error.message, error.message!!.contains("J1 No. 1"))
    }

    @Test
    fun aHintForAPinTheGuideLacksStops() {
        val error =
            assertThrows(ImportException::class.java) {
                Hints.read(HINTS + "  - { table: J1, no: 9, safe: ok }\n").check(tables)
            }
        assertTrue(error.message, error.message!!.contains("J1 No. 9"))
    }

    @Test
    fun aPinHintedTwiceStops() {
        assertThrows(ImportException::class.java) {
            Hints.read(HINTS + "  - { table: J1, no: 2, safe: ok }\n").check(tables)
        }
    }

    @Test
    fun aGuideTableWithoutAHeaderHintStops() {
        val error =
            assertThrows(ImportException::class.java) {
                Hints.read(HINTS).check(tables + PinTable("J3", listOf(TableRow(1, "G", "G", "Ground", emptySet()))))
            }
        assertTrue(error.message, error.message!!.contains("J3"))
    }

    companion object {
        // A made-up board. s1 and s2 are added by the importer (the dataset and the guide).
        val HINTS =
            """
            part:
              id: example/board
              name: Example Board
              manufacturer: Example
              category: esp32-boards
              summary: A made-up board for the importer's tests.
            guide: { ref: docs/en/board/user_guide.rst, url: "https://example.com/board.html", version: v1.0 }
            sources:
              - { id: s3, type: schematic, title: Example schematic, publisher: Example, url: "https://example.com/sch.pdf", retrieved: 2026-10-31, license: link-only }
            headers:
              - table: J1
                id: j1
                label: J1
                type: header
                pitch_mm: 2.54
                source: s3
                page: 1
                edges:
                  - { edge: left, pins: 3, first: top, source: s3, page: 1 }
            pins:
              - { table: J1, no: 1, safe: caution, cites: [ { source: s3, page: 2 } ] }
              - { table: J1, no: 2, safe: ok, board_label: "4", cites: [ { source: s3, page: 1 } ] }
              - { table: J1, no: 3, safe: ok }
            """.trimIndent() + "\n"
    }
}
