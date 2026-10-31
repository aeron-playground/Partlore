package dev.partlore.tools.importer.rst

/**
 * Where a guide names a GPIO outside its pin tables: in a note, a warning or a footnote. Such text
 * often says a pin is taken or risky (for example by the module's PSRAM) without marking the row.
 */
object GuideNotes {
    private val START =
        Regex("""^\.\. ((note|attention|warning|important|caution|danger)::|\[(#\w*|\d+)]).*$""")

    // RTC_GPIO4 is a function name, not the pin GPIO4: `_` is a word character, so \b doesn't match there.
    private val GPIO = Regex("""\bGPIO\d+\b""")

    /** GPIO name → the lines (from 1) where the notes that name it start. */
    fun pinsNamed(rst: String): Map<String, List<Int>> {
        val lines = rst.lines()
        val found = sortedMapOf<String, MutableList<Int>>()
        var i = 0
        while (i < lines.size) {
            if (!START.matches(lines[i].trim())) {
                i++
                continue
            }
            val indent = indentOf(lines[i])
            var end = i + 1
            while (end < lines.size && (lines[end].isBlank() || indentOf(lines[end]) > indent)) end++
            val named = (i until end).flatMap { n -> GPIO.findAll(lines[n]).map { it.value }.toList() }.toSet()
            named.forEach { found.getOrPut(it) { mutableListOf() } += i + 1 }
            i = end
        }
        return found
    }

    private fun indentOf(line: String): Int = line.length - line.trimStart().length
}
