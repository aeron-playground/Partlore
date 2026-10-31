package dev.partlore.tools.importer

import dev.partlore.tools.importer.YamlOut.inline
import dev.partlore.tools.importer.YamlOut.quote
import dev.partlore.tools.importer.YamlOut.text
import dev.partlore.tools.importer.rst.PinTable
import dev.partlore.tools.importer.rst.PinTables
import dev.partlore.tools.importer.rst.TableRow
import tools.jackson.databind.JsonNode
import java.time.LocalDate

/** The files of one imported board (file name → YAML text) and what the person checking it must look at. */
data class ImportResult(val files: Map<String, String>, val flagged: List<String>)

/**
 * Builds part.yaml, sources.yaml and pins.yaml for a board from its esp-dev-kits user guide and its
 * hints. The guide's pin tables give the facts; the hints give everything else. The files are
 * `imported`, so their pins ship only after a person checks them.
 */
object Importer {
    const val NAME = "esp-dev-kits"
    private const val DATASET = "s1"
    private const val GUIDE = "s2"

    /** Every file the importer writes starts with this, so it can tell its own files from a person's. */
    const val MARKER = "# Written by tools:importer"
    private const val NOTICE =
        "$MARKER from Espressif's esp-dev-kits. To change it, edit the board's hints\n" +
            "# file and import again.\n"

    fun import(guide: String, hints: Hints, commit: String, on: LocalDate): ImportResult {
        hints.sources.firstOrNull { it.get("id")?.asString() in setOf(DATASET, GUIDE) }?.let {
            throw ImportException("hint sources can't use the ids $DATASET and $GUIDE: the importer writes those")
        }
        val tables = PinTables.read(guide)
        hints.check(tables)
        val pins = pins(tables, hints)
        val origin = "{ importer: $NAME, source: $DATASET, ref: ${text(hints.guide.ref)}, on: $on }"
        return ImportResult(
            files =
            mapOf(
                "part.yaml" to part(hints, origin),
                "sources.yaml" to sources(hints, commit, on),
                "pins.yaml" to pinFile(hints, tables, pins, origin),
            ),
            flagged = flagged(pins),
        )
    }

    private class Pin(
        val table: PinTable,
        val row: TableRow,
        val facts: PinFacts,
        val hint: JsonNode,
        val header: String,
    ) {
        val reserved = hint.get("reserved")?.booleanValue() == true
        val chipName = facts.chipName.takeUnless { reserved }
        val base =
            when {
                chipName != null -> chipName.lowercase()
                facts.direction == "ground" -> "gnd"
                else -> row.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            }
        var id = base
    }

    // Ids follow the checked files: gpio4, 3v3, and gnd-j1-22 when a name repeats.
    private fun pins(tables: List<PinTable>, hints: Hints): List<Pin> {
        val pins =
            tables.flatMap { table ->
                val header = hints.headers.first { it.table == table.heading }.node.get("id").asString()
                table.rows.map {
                    Pin(table, it, Signals.read(it.type, it.function), hints.pin(table.heading, it.no), header)
                }
            }
        val repeated = pins.groupingBy { it.base }.eachCount().filterValues { it > 1 }.keys
        pins.filter { it.base in repeated }.forEach { it.id = "${it.base}-${it.header}-${it.row.no}" }
        return pins
    }

    private fun part(hints: Hints, origin: String): String = buildString {
        append("# yaml-language-server: \$schema=../../../schema/part.schema.json\n").append(NOTICE)
        val part = hints.part
        append("id: ${text(part.get("id").asString())}\n")
        append("kind: board\n")
        append("name: ${text(part.get("name").asString())}\n")
        part.get("aliases")?.let { append("aliases: ${inline(it)}\n") }
        append("manufacturer: ${text(part.get("manufacturer").asString())}\n")
        append("category: ${text(part.get("category").asString())}\n")
        part.get("tags")?.let { append("tags: ${inline(it)}\n") }
        append("summary: ${quote(part.get("summary").asString())}\n")
        append("status:\n  level: imported\n  imported_from: $origin\n")
    }

    private fun sources(hints: Hints, commit: String, on: LocalDate): String = buildString {
        append("# yaml-language-server: \$schema=../../../schema/sources.schema.json\n").append(NOTICE)
        append("sources:\n")
        source(
            "id" to DATASET,
            "type" to "dataset",
            "title" to "esp-dev-kits",
            "publisher" to "Espressif Systems",
            "url" to "https://github.com/espressif/esp-dev-kits",
            "version" to commit,
            "retrieved" to "$on",
            "license" to "Apache-2.0",
        )
        source(
            "id" to GUIDE,
            "type" to "web-doc",
            "title" to "${hints.part.get("name").asString()} user guide",
            "publisher" to "Espressif Systems",
            "url" to hints.guide.url,
            "version" to "${hints.guide.version}, source commit ${commit.take(SHORT_COMMIT)}",
            "retrieved" to "$on",
            "license" to "link-only",
        )
        for (node in hints.sources) {
            node.properties().forEachIndexed { i, (key, value) ->
                append(
                    if (i ==
                        0
                    ) {
                        "  - "
                    } else {
                        "    "
                    },
                ).append("$key: ${inline(value)}\n")
            }
        }
    }

    private fun StringBuilder.source(vararg fields: Pair<String, String>) {
        fields.forEachIndexed { i, (key, value) ->
            append(
                if (i ==
                    0
                ) {
                    "  - "
                } else {
                    "    "
                },
            ).append("$key: ${text(value)}\n")
        }
    }

    private fun pinFile(hints: Hints, tables: List<PinTable>, pins: List<Pin>, origin: String): String = buildString {
        append("# yaml-language-server: \$schema=../../../schema/pins.schema.json\n").append(NOTICE)
        append("status:\n  level: imported\n  imported_from: $origin\n")
        append("headers:\n")
        for (table in tables) {
            val header = hints.headers.first { it.table == table.heading }.node
            append("  - id: ${inline(header.get("id"))}\n")
            append("    label: ${inline(header.get("label"))}\n")
            append("    type: ${inline(header.get("type"))}\n")
            append("    rows: 1\n")
            append("    pins_per_row: ${table.rows.size}\n")
            for (key in listOf("pitch_mm", "source", "page", "section", "ref")) {
                header.get(key)?.let { append("    $key: ${inline(it)}\n") }
            }
            append("    edges:\n")
            header.get("edges").values().forEach { append("      - ${inline(it)}\n") }
        }
        append("pins:\n")
        pins.forEach { append(pin(it, hints.guide.ref)) }
    }

    // Fields in the order the checked files use.
    private fun pin(pin: Pin, guideRef: String): String = buildString {
        val hint = pin.hint
        append("  - id: ${pin.id}\n")
        append("    header: ${pin.header}\n    row: 1\n    index: ${pin.row.no}\n")
        pin.chipName?.let { append("    chip_name: ${text(it)}\n") }
        hinted(hint, "module_pad")
        append("    board_label: ${quote(hint.get("board_label")?.asString() ?: pin.row.name)}\n")
        hinted(hint, "label_aliases", "arduino")
        append("    direction: ${if (pin.reserved) "nc" else pin.facts.direction}\n")
        hinted(hint, "voltage", "five_v_tolerant")
        if (!pin.reserved) functions(pin.facts.functions)
        hinted(hint, "strapping", "safe")
        hint.get("note")?.let { append("    note: ${quote(it.asString())}\n") }
        append("    cites:\n")
        append("      - { source: $DATASET, ref: ${quote("$guideRef, ${pin.table.heading} No. ${pin.row.no}")} }\n")
        append("      - { source: $GUIDE, section: ${text(pin.table.heading)} }\n")
        hint.get("cites")?.values()?.forEach { append("      - ${inline(it)}\n") }
    }

    // Copies the hint's own value for each key it has.
    private fun StringBuilder.hinted(hint: JsonNode, vararg keys: String) {
        for (key in keys) hint.get(key)?.let { append("    $key: ${inline(it)}\n") }
    }

    private fun StringBuilder.functions(functions: List<Fn>) {
        if (functions.isEmpty()) return
        append("    functions:\n")
        for (fn in functions) {
            append("      - { type: ${fn.type}${fn.signal?.let { ", signal: ${text(it)}" }.orEmpty()} }\n")
        }
    }

    private fun flagged(pins: List<Pin>): List<String> = listOf(
        "Board labels: each board label is the guide's Name unless a hint sets it. Compare them with the board photo.",
    ) +
        pins.flatMap { pin ->
            val where = "${pin.table.heading} No. ${pin.row.no} (${pin.chipName ?: pin.row.name})"
            pin.row.footnotes.sorted().map {
                "$where: the guide has footnote $it here. Read it; put what matters in the hints."
            } +
                pin.facts.flags.map { "$where: '$it' is not a pin function. Say it in a note if it matters." }
        }

    private const val SHORT_COMMIT = 7
}
