package dev.partlore.tools.importer

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.diag.Severity
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import tools.jackson.databind.JsonNode
import tools.jackson.dataformat.yaml.YAMLMapper
import java.time.LocalDate

class ImporterTest {
    @get:Rule val folder = TemporaryFolder()

    private val on = LocalDate.of(2026, 10, 31)
    private val commit = "bd4d4ac0000000000000000000000000000000aa"

    private fun import(hints: String = HINTS, guide: String = GUIDE) =
        Importer.import(guide, Hints.read(hints), commit, on)

    private fun pins(result: ImportResult): Map<String, JsonNode> =
        YAMLMapper().readTree(result.files.getValue("pins.yaml")).get("pins").values().associateBy {
            it.get("id").asString()
        }

    @Test
    fun importedFilesPassEveryContentRuleInReleaseAndPreview() {
        val content = ContentFixture(folder.root)
        import().files.forEach { (name, text) -> content.write("parts/example/board/$name", text) }
        for (mode in Mode.entries) {
            val result = Validator(folder.root).validate(mode)
            val problems = result.diagnostics.filter { it.severity == Severity.ERROR }
            assertEquals("$mode: $problems", emptyList<Any>(), problems)
            // No files would also mean no problems: the part and its 4 pins must really be there.
            assertEquals(4, result.content.parts.single { it.id == "example/board" }.pins?.pins?.size)
        }
    }

    @Test
    fun theSameInputGivesTheSameFiles() {
        assertEquals(import().files, import().files)
    }

    @Test
    fun pinsCarryTheGuideFactsAndTheHints() {
        val pins = pins(import())
        assertEquals(listOf("3v3", "gpio4", "gnd-j1-3", "gnd-j1-4"), pins.keys.toList())
        val gpio4 = pins.getValue("gpio4")
        assertEquals("GPIO4", gpio4.get("chip_name").asString())
        assertEquals("4", gpio4.get("board_label").asString())
        assertEquals("io", gpio4.get("direction").asString())
        assertEquals(
            listOf("gpio" to null, "adc" to "ADC1_CH3"),
            gpio4.get("functions").values().map { it.get("type").asString() to it.get("signal")?.asString() },
        )
        assertEquals("ok", gpio4.get("safe").asString())
        assertEquals(
            listOf("s1", "s2", "s3"),
            gpio4.get("cites").values().map { it.get("source").asString() },
        )
        assertEquals(
            "docs/en/board/user_guide.rst, J1 No. 2",
            gpio4.get("cites").values().first().get("ref").asString(),
        )
        // Without a hint, the board label is the guide's Name.
        assertEquals("3V3", pins.getValue("3v3").get("board_label").asString())
    }

    @Test
    fun aReservedPinHasNoChipNameAndNoFunctions() {
        val hints =
            HINTS.replace(
                "safe: ok, board_label: \"4\", voltage: 3.3, five_v_tolerant: false",
                "safe: avoid, reserved: true",
            )
        val pin = pins(import(hints)).getValue("io4")
        assertEquals("nc", pin.get("direction").asString())
        assertTrue(pin.get("chip_name") == null && pin.get("functions") == null)
        assertEquals("avoid", pin.get("safe").asString())
    }

    @Test
    fun bothFilesSayImportedAndWhereFrom() {
        val files = import().files
        for (name in listOf("part.yaml", "pins.yaml")) {
            val status = YAMLMapper().readTree(files.getValue(name)).get("status")
            assertEquals(name, "imported", status.get("level").asString())
            val from = status.get("imported_from")
            assertEquals(
                listOf("esp-dev-kits", "s1", "docs/en/board/user_guide.rst", "2026-10-31"),
                listOf("importer", "source", "ref", "on").map { from.get(it).asString() },
            )
        }
        val sources = YAMLMapper().readTree(files.getValue("sources.yaml")).get("sources").values().toList()
        assertEquals(listOf("s1", "s2", "s3"), sources.map { it.get("id").asString() })
        assertEquals(commit, sources[0].get("version").asString())
        assertEquals("link-only", sources[1].get("license").asString())
    }

    @Test
    fun footnotesAndBoardThingsAreListedForThePersonChecking() {
        val flagged = import().flagged
        assertTrue(flagged.toString(), flagged.any { "J1 No. 2" in it && "footnote 2" in it })
        assertTrue(flagged.toString(), flagged.any { "board label" in it })
    }

    @Test
    fun pinsNamedInANoteAreListedEvenWithoutAFootnote() {
        val guide = GUIDE + "\n.. note::\n\n    On some modules GPIO4 is used inside the module.\n"
        val flagged = import(guide = guide).flagged
        assertTrue(flagged.toString(), flagged.any { "J1 No. 2" in it && "note at line 13" in it })
    }

    @Test
    fun hintSourcesMayNotTakeTheImportersIds() {
        assertThrows(ImportException::class.java) { import(HINTS.replace("- id: s3", "- id: s2")) }
    }

    companion object {
        // Made up: not copied from any guide.
        val GUIDE =
            """
            J1
            ^^^

            ===  ====  ==========  ====================
            No.  Name  Type [#]_   Function
            ===  ====  ==========  ====================
            1    3V3   P           3.3 V power supply
            2    IO4   I/O/T       GPIO4, ADC1_CH3 [2]_
            3    G     G           Ground
            4    G     G           Ground
            ===  ====  ==========  ====================
            """.trimIndent() + "\n"

        val HINTS =
            """
            part:
              id: example/board
              name: Example Board
              manufacturer: Example
              category: test-boards
              tags: [test-tag]
              summary: A made-up board for the importer's tests.
            guide: { ref: docs/en/board/user_guide.rst, url: "https://example.com/board.html", version: v1.0 }
            sources:
              - id: s3
                type: schematic
                title: Example schematic
                publisher: Example
                url: "https://example.com/sch.pdf"
                version: "1.0"
                retrieved: 2026-10-31
                sha256: "0000000000000000000000000000000000000000000000000000000000000000"
                license: link-only
            headers:
              - table: J1
                id: j1
                label: J1
                type: header
                rows: 1
                pitch_mm: 2.54
                source: s3
                page: 1
                edges:
                  - { edge: left, pins: 4, first: top, source: s3, page: 1 }
            pins:
              - { table: J1, no: 1, safe: caution, voltage: 3.3, cites: [ { source: s3, page: 2 } ] }
              - { table: J1, no: 2, safe: ok, board_label: "4", voltage: 3.3, five_v_tolerant: false, cites: [ { source: s3, page: 1 } ] }
              - { table: J1, no: 3, safe: ok }
              - { table: J1, no: 4, safe: ok }
            """.trimIndent() + "\n"
    }
}
