package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Pos

/** Source types that are PDF documents: their citations need a page. */
val PDF_TYPES: Set<String> =
    setOf("datasheet", "reference-manual", "errata", "app-note", "user-guide", "schematic", "pinout")

/** Lowercase; every run of other characters becomes one "-"; no "-" at the ends. */
fun slug(text: String): String = text.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

internal fun <T> reportDuplicates(items: List<T>, key: (T) -> String, pos: (T) -> Pos, what: String, d: Diagnostics) {
    items.groupBy(key).filterValues { it.size > 1 }.forEach { (value, copies) ->
        copies.drop(1).forEach { d.error(pos(it), "$what \"$value\" is listed twice") }
    }
}
