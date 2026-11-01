package dev.partlore.core.content

import androidx.sqlite.SQLiteConnection
import dev.partlore.core.model.GotchaHit
import dev.partlore.core.model.PartHit
import dev.partlore.core.model.PinHit
import dev.partlore.core.model.SearchQuery
import dev.partlore.core.model.SearchResults
import dev.partlore.core.model.Severity
import dev.partlore.core.model.VerificationLevel

private const val LIMIT = 30

// Several names of one pin can match, so a few more rows than pins are read.
private const val PIN_ROWS = 200

// A pin name is looked for inside at most this many parts; never scan every pin of a big maker.
private const val SCOPE_LIMIT = 100

// The search index works in 3-letter pieces: it finds nothing for a shorter word, without any error.
private const val INDEX_MIN_LETTERS = 3
private const val NUMBER_WIDTH = 6
private val NUMBER = Regex("[0-9]+")

// One word against one part, in a query over `part p`. Each ? takes the word's LIKE pattern.
private const val PART_WORD_ARGS = 3
private const val PART_WORD =
    "(p.name LIKE ? ESCAPE '\\' OR p.manufacturer LIKE ? ESCAPE '\\' " +
        "OR p.id IN (SELECT part_id FROM part_alias WHERE alias LIKE ? ESCAPE '\\'))"

private const val GOTCHA_WORD_ARGS = 2
private const val GOTCHA_WORD = "(g.title LIKE ? ESCAPE '\\' OR g.body LIKE ? ESCAPE '\\')"

/**
 * Answers one search (ADR 0014). A part must match every word. A pin is named by one word, and every
 * other word names its part, so "esp32 gpio 34" is pin GPIO34 on the parts called esp32.
 */
internal class SearchQueries(private val db: SQLiteConnection) {
    private var scopeCut = false

    fun search(text: String): SearchResults {
        val words = SearchQuery.parse(text).words
        if (words.isEmpty()) return SearchResults()
        val parts = parts(words)
        val pins = pins(words)
        val gotchas = gotchas(words)
        return SearchResults(
            parts = parts.take(LIMIT),
            pins = pins.take(LIMIT),
            gotchas = gotchas.take(LIMIT),
            more = scopeCut || listOf(parts, pins, gotchas).any { it.size > LIMIT },
        )
    }

    /** The best name matches first. Levels and aliases are read only for the parts that will show. */
    private fun parts(words: List<String>): List<PartHit> {
        val phrase = words.joinToString(" ")
        val rows =
            db.query(
                "SELECT p.id, p.name, p.manufacturer, p.kind FROM part p WHERE ${partWhere(words)}",
                partArgs(words),
            ) { PartRow(it.text(), it.text(), it.text(), it.text()) }
                .sortedWith(compareBy({ it.rank(phrase, words) }, { it.name.lowercase() }, { it.id }))
                .take(LIMIT + 1)
        val ids = rows.map { it.id }
        val levels =
            db.query("SELECT part_id, level FROM status WHERE part_id IN (${marks(ids)})", ids) {
                it.text() to VerificationLevel.fromPack(it.text())
            }.groupBy({ it.first }, { it.second })
        val aliases =
            db.query("SELECT part_id, alias FROM part_alias WHERE part_id IN (${marks(ids)})", ids) {
                it.text() to it.text()
            }.groupBy({ it.first }, { it.second })
        return rows.map { row ->
            PartHit(
                id = row.id,
                name = row.name,
                manufacturer = row.manufacturer,
                kind = row.kind,
                level = VerificationLevel.weakestOf(levels[row.id].orEmpty()),
                alias = row.matchedAlias(words, aliases[row.id].orEmpty()),
            )
        }
    }

    private fun pins(words: List<String>): List<PinHit> {
        val rows =
            (
                words.indices.flatMap { exactPins(words[it], words.without(it)) } +
                    words.indices.flatMap { pinsContaining(words[it], words.without(it)) }
                )
                .distinctBy { it.partId to it.pinId }
        val partIds = rows.map { it.partId }.distinct()
        val partNames =
            db.query("SELECT id, name FROM part WHERE id IN (${marks(partIds)})", partIds) {
                it.text() to it.text()
            }.toMap()
        return rows
            .sortedWith(
                compareBy(
                    { !it.exact },
                    { partNames[it.partId].orEmpty().lowercase() },
                    { it.partId },
                    { naturalKey(it.label) },
                ),
            )
            .take(LIMIT + 1)
            .map { row ->
                PinHit(
                    partId = row.partId,
                    partName = partNames[row.partId].orEmpty(),
                    pinId = row.pinId,
                    label = row.label,
                    otherNames = otherNames(row),
                    exact = row.exact,
                )
            }
    }

    /** Pins with exactly this name, through the index on names, so a 2-letter name is as fast as any. */
    private fun exactPins(word: String, others: List<String>): List<PinRow> {
        val labels = labelsFor(word)
        val scope = if (others.isEmpty()) "" else "AND part_id IN (SELECT p.id FROM part p WHERE ${partWhere(others)})"
        return db.query(
            "SELECT part_id, pin_id, min(label) FROM pin_alias WHERE label_lower IN (${marks(labels)}) $scope " +
                "GROUP BY part_id, pin_id ORDER BY part_id, pin_id LIMIT ${LIMIT + 1}",
            labels + partArgs(others),
        ) { PinRow(it.text(), it.text(), it.text(), exact = true) }
    }

    /** Pins with this word inside a name, from three letters up. */
    private fun pinsContaining(word: String, others: List<String>): List<PinRow> = when {
        word.codePointCount(0, word.length) < INDEX_MIN_LETTERS -> emptyList()

        others.isEmpty() ->
            db.query(
                "SELECT part_id, ref, text FROM search WHERE search MATCH ? AND kind = 'pin' LIMIT $PIN_ROWS",
                listOf(phraseOf(word)),
            ) { PinRow(it.text(), it.text(), it.text(), exact = false) }

        else -> pinsOfParts(word, others)
    }

    private fun pinsOfParts(word: String, others: List<String>): List<PinRow> {
        val scope =
            db.query(
                "SELECT p.id FROM part p WHERE ${partWhere(others)} ORDER BY p.id LIMIT ${SCOPE_LIMIT + 1}",
                partArgs(others),
            ) { it.text() }
        if (scope.size > SCOPE_LIMIT) scopeCut = true
        val ids = scope.take(SCOPE_LIMIT)
        return db.query(
            "SELECT part_id, pin_id, label FROM pin_alias WHERE part_id IN (${marks(ids)}) " +
                "AND label_lower LIKE ? ESCAPE '\\' LIMIT $PIN_ROWS",
            ids + patternOf(word),
        ) { PinRow(it.text(), it.text(), it.text(), exact = false) }
    }

    private fun otherNames(row: PinRow): List<String> =
        db.query("SELECT label FROM pin_alias WHERE part_id = ? AND pin_id = ?", listOf(row.partId, row.pinId)) {
            it.text()
        }
            .filter { it != row.label }
            // A bare Arduino number says least, so it goes last.
            .sortedBy { SearchQuery.isNumber(it) }

    /** Every word is in the gotcha or names its part, and at least one is in the gotcha. Danger first. */
    private fun gotchas(words: List<String>): List<GotchaHit> {
        val own = words.joinToString(" OR ") { GOTCHA_WORD }
        val ownArgs = words.flatMap { word -> List(GOTCHA_WORD_ARGS) { patternOf(word) } }
        // With one word, "in the gotcha" already says it all.
        val every =
            if (words.size == 1) "" else " AND " + words.joinToString(" AND ") { "($GOTCHA_WORD OR $PART_WORD)" }
        val everyArgs =
            if (words.size == 1) {
                emptyList()
            } else {
                words.flatMap { word -> List(GOTCHA_WORD_ARGS + PART_WORD_ARGS) { patternOf(word) } }
            }
        return db.query(
            "SELECT g.part_id, p.name, g.id, g.title, g.severity FROM gotcha g JOIN part p ON p.id = g.part_id " +
                "WHERE ($own)$every " +
                "ORDER BY CASE g.severity WHEN 'danger' THEN 0 WHEN 'info' THEN 2 ELSE 1 END, " +
                "p.name COLLATE NOCASE, g.part_id, g.id LIMIT ${LIMIT + 1}",
            ownArgs + everyArgs,
        ) { GotchaHit(it.text(), it.text(), it.text(), it.text(), Severity.fromPack(it.text())) }
    }

    private data class PartRow(val id: String, val name: String, val manufacturer: String, val kind: String) {
        /** 0: the whole name. 1: the start of the name. 2: every word in the name. 3: only the maker or an alias. */
        fun rank(phrase: String, words: List<String>): Int {
            val lower = name.lowercase()
            return when {
                lower == phrase -> 0
                lower.startsWith(phrase) -> 1
                words.all { it in lower } -> 2
                else -> 3
            }
        }

        /** The alias to show: only when the name and maker don't explain why this part matched. */
        fun matchedAlias(words: List<String>, aliases: List<String>): String? {
            val unexplained = words.filterNot { it in name.lowercase() || it in manufacturer.lowercase() }
            return aliases.firstOrNull { alias -> unexplained.any { it in alias.lowercase() } }
        }
    }

    private data class PinRow(val partId: String, val pinId: String, val label: String, val exact: Boolean)

    private companion object {
        fun List<String>.without(index: Int) = filterIndexed { i, _ -> i != index }

        fun partWhere(words: List<String>) = words.joinToString(" AND ") { PART_WORD }

        fun partArgs(words: List<String>) = words.flatMap { word -> List(PART_WORD_ARGS) { patternOf(word) } }

        fun marks(items: List<*>) = items.joinToString { "?" }

        /** The names a word stands for. A plain number is a pin number: 34 also means GPIO34, IO34, D34… */
        fun labelsFor(word: String): List<String> = if (SearchQuery.isNumber(word)) {
            listOf(word) + SearchQuery.PIN_PREFIXES.map { it + word }
        } else {
            listOf(word)
        }

        /** "Contains this word" for LIKE: `%`, `_` and `\` are ordinary letters here. */
        fun patternOf(word: String): String {
            val escaped = word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
            return "%$escaped%"
        }

        /** One quoted phrase for the index, so `"`, `*` or AND in the query are not search commands. */
        fun phraseOf(word: String): String = "\"" + word.replace("\"", "\"\"") + "\""

        /** Sorts GPIO2 before GPIO10: numbers are padded so text order is number order. */
        fun naturalKey(label: String): String =
            NUMBER.replace(label.lowercase()) { it.value.padStart(NUMBER_WIDTH, '0') }
    }
}
