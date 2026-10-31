package dev.partlore.tools.importer.rst

import dev.partlore.tools.importer.ImportException

/** One pin row as the guide writes it. [footnotes] are the labels of the footnote refs in its cells. */
data class TableRow(val no: Int, val name: String, val type: String, val function: String, val footnotes: Set<String>)

/** A pin table and the heading it sits under (the header's name, for example J1). */
data class PinTable(val heading: String, val rows: List<TableRow>)

/**
 * Reads the pin tables of a board user guide (reStructuredText): every table whose columns are
 * No., Name, Type and Function, in either form the guides use (a simple table or a list-table).
 */
object PinTables {
    private val COLUMNS = listOf("No.", "Name", "Type", "Function")
    private val SIMPLE_BORDER = Regex("""^=+( +=+)+\s*$""")
    private val UNDERLINE = Regex("""^([-=^~"'`#*+])\1{2,}\s*$""")
    private val FOOTNOTE = Regex("""\s*\[(#\w*|\d+)]_""")

    fun read(rst: String): List<PinTable> {
        val lines = rst.lines()
        val tables = mutableListOf<PinTable>()
        var heading = ""
        var i = 0
        while (i < lines.size) {
            val found =
                when {
                    isHeading(lines, i) -> {
                        heading = lines[i].trim()
                        null to i + 2
                    }

                    SIMPLE_BORDER.matches(lines[i]) -> simpleTable(lines, i)

                    lines[i].trim() == ".. list-table::" -> listTable(lines, i)

                    else -> null to i + 1
                }
            found.first?.let { tables += PinTable(heading, it) }
            i = found.second
        }
        return tables
    }

    // A section title: a line of text, then a line of one repeated punctuation mark at least as long.
    private fun isHeading(lines: List<String>, i: Int): Boolean {
        val text = lines[i].trim()
        val under = lines.getOrNull(i + 1) ?: return false
        return text.isNotEmpty() && !UNDERLINE.matches(lines[i]) && UNDERLINE.matches(under) &&
            under.trim().length >= text.length
    }

    // Returns the pin rows (null when it is another kind of table) and the line after the table.
    private fun simpleTable(lines: List<String>, start: Int): Pair<List<TableRow>?, Int> {
        val starts = Regex("=+").findAll(lines[start]).map { it.range.first }.toList()
        val headerEnd =
            (start + 1 until lines.size).firstOrNull { SIMPLE_BORDER.matches(lines[it]) } ?: return null to start + 1
        val end = (headerEnd + 1 until lines.size).firstOrNull { SIMPLE_BORDER.matches(lines[it]) } ?: lines.size
        val header = cells(lines[start + 1], starts).map(::clean)
        if (header != COLUMNS) return null to end + 1
        val rows = mutableListOf<Pair<Int, MutableList<String>>>()
        for (n in headerEnd + 1 until end) {
            if (lines[n].isBlank()) continue
            val cells = cells(lines[n], starts)
            val previous = rows.lastOrNull()
            if (cells[0].isBlank() && previous != null) {
                cells.forEachIndexed { k, text -> if (text.isNotBlank()) previous.second[k] += " $text" }
            } else {
                rows += n to cells.toMutableList()
            }
        }
        return rows.map { (n, cells) -> row(n, cells) } to end + 1
    }

    private fun cells(line: String, starts: List<Int>): List<String> = starts.mapIndexed { k, from ->
        val to = starts.getOrNull(k + 1) ?: line.length
        if (from >= line.length) "" else line.substring(from, minOf(to, line.length)).trim()
    }

    private fun listTable(lines: List<String>, start: Int): Pair<List<TableRow>?, Int> {
        val indent = indentOf(lines[start])
        var headerRows = 0
        val rows = mutableListOf<Pair<Int, MutableList<String>>>()
        var n = start + 1
        while (n < lines.size && (lines[n].isBlank() || indentOf(lines[n]) > indent)) {
            val text = lines[n].trim()
            when {
                text.startsWith(":header-rows:") ->
                    headerRows = text.substringAfter(":header-rows:").trim().toIntOrNull() ?: 0

                text.startsWith("* - ") || text == "* -" -> rows += n to mutableListOf(text.removePrefix("* -").trim())

                text.startsWith("- ") || text == "-" -> rows.lastOrNull()?.second?.add(text.removePrefix("-").trim())

                text.isNotEmpty() && !text.startsWith(":") ->
                    rows.lastOrNull()?.second?.let { it[it.lastIndex] += " $text" }
            }
            n++
        }
        val header = rows.firstOrNull()?.second?.map(::clean)
        if (headerRows < 1 || header != COLUMNS) return null to n
        return rows.drop(headerRows).map { (line, cells) -> row(line, cells) } to n
    }

    private fun row(line: Int, cells: List<String>): TableRow {
        if (cells.size != COLUMNS.size) {
            throw ImportException("line ${line + 1}: a pin row has ${cells.size} cells, not ${COLUMNS.size}")
        }
        val no = clean(cells[0])
        val number = no.toIntOrNull() ?: throw ImportException("line ${line + 1}: pin number '$no' is not a number")
        val footnotes = cells.flatMap { cell -> FOOTNOTE.findAll(cell).map { it.groupValues[1] } }.toSet()
        return TableRow(number, clean(cells[1]), clean(cells[2]), clean(cells[3]), footnotes)
    }

    private fun clean(cell: String): String = cell.replace(FOOTNOTE, "").trim()

    private fun indentOf(line: String): Int = line.length - line.trimStart().length
}
