package dev.partlore.tools.importer

import dev.partlore.tools.importer.rst.PinTables
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import tools.jackson.databind.JsonNode
import tools.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import java.time.LocalDate

/**
 * Imports the ESP32-DevKitC V4 from its guide at the commit the checked pins.yaml cites, and compares
 * everything the importer works out by itself with that checked file. The hints get only what a person
 * decides (safe, reserved flash pins, board labels the guide names differently), so it can't be circular.
 *
 * Runs only with `-PespDevKits=<esp-dev-kits checkout>` (no network in CI); skipped otherwise.
 */
class GoldenDevKitCTest {
    private val checkout = System.getProperty("espDevKits")
    private val contentDir = File(System.getProperty("partlore.contentDir") ?: "content")
    private val yaml = YAMLMapper()

    @Test
    fun theImportMatchesTheCheckedDevKitC() {
        assumeTrue("set -PespDevKits=<esp-dev-kits checkout> to run the golden test", checkout != null)
        val guide = git("show", "$CITED_COMMIT:$GUIDE")
        val checked = yaml.readTree(File(contentDir, "parts/$PART/pins.yaml").readText())
        val rows = PinTables.read(guide).flatMap { t -> t.rows.map { (t.heading to it.no) to it } }.toMap()

        val result = Importer.import(
            guide,
            Hints.read(
                hints(
                    checked,
                    rows.mapValues {
                        it.value.name
                    },
                ),
            ),
            CITED_COMMIT,
            LocalDate.of(2026, 10, 31),
        )
        val imported = yaml.readTree(result.files.getValue("pins.yaml"))

        assertEquals(ids(checked), ids(imported))
        val wanted = checked.get("pins").values().associateBy { it.get("id").asString() }
        val differences =
            imported.get("pins").values().flatMap { pin ->
                val id = pin.get("id").asString()
                FIELDS.mapNotNull { field ->
                    val got = facts(pin, field)
                    val expected = facts(wanted.getValue(id), field)
                    "$id $field: imported $got, checked $expected".takeIf { got != expected }
                }
            }
        assertEquals(differences.joinToString("\n"), emptyList<String>(), differences)
    }

    // Only what a person decides: safe, the flash pins the checked file reserves, and board labels that differ.
    private fun hints(checked: JsonNode, names: Map<Pair<String, Int>, String>): String = buildString {
        append(
            "part: { id: $PART, name: ESP32-DevKitC V4, manufacturer: Espressif, category: esp32-boards, " +
                "summary: golden }\n",
        )
        append("guide: { ref: $GUIDE, url: \"https://example.com/guide\", version: latest }\n")
        append("headers:\n")
        val labels = checked.get("headers").values().associate { it.get("id").asString() to it.get("label").asString() }
        for (header in checked.get("headers").values()) {
            append("  - table: ${header.get("label").asString()}\n")
            for (key in listOf("id", "label", "type", "rows", "pitch_mm", "source", "page", "edges")) {
                append("    $key: ${YamlOut.inline(header.get(key))}\n")
            }
        }
        append("pins:\n")
        for (pin in checked.get("pins").values()) {
            val table = labels.getValue(pin.get("header").asString())
            val no = pin.get("index").intValue()
            val label = pin.get("board_label").asString()
            append(
                "  - { table: $table, no: $no, safe: ${pin.get("safe").asString()}, cites: [ { source: s3, page: 1 } ]",
            )
            if (pin.get("direction").asString() == "nc") append(", reserved: true")
            if (names[table to no] != label) append(", board_label: ${YamlOut.quote(label)}")
            append(" }\n")
        }
    }

    private fun ids(pins: JsonNode): List<String> = pins.get("pins").values().map { it.get("id").asString() }

    private fun facts(pin: JsonNode, field: String): Any? = when (field) {
        "functions" -> pin.get("functions")?.values()?.map {
            it.get("type").asString() to it.get("signal")?.asString()
        }.orEmpty()

        else -> pin.get(field)?.asString()
    }

    private fun git(vararg args: String): String {
        val process = ProcessBuilder("git", "-C", checkout, *args).start()
        val text = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { process.errorStream.bufferedReader().readText() }
        return text
    }

    private companion object {
        const val PART = "espressif/esp32-devkitc-v4-wroom-32e"
        const val GUIDE = "docs/en/esp32-devkitc/user_guide.rst"

        // The commit the checked file's guide source (s2) names.
        const val CITED_COMMIT = "52772fb"

        // What the importer works out itself. safe, reserved and board labels come from the hints.
        val FIELDS = listOf("header", "index", "chip_name", "direction", "functions")
    }
}
