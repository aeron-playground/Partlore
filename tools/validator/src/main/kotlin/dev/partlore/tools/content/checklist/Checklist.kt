package dev.partlore.tools.content.checklist

import dev.partlore.tools.content.model.Citation
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Source

/** One value to compare against its source. */
internal data class Item(val cite: Citation, val file: String, val line: Int, val text: String)

/** Every cited value of a part, grouped by source and page, so a reviewer opens each page once. */
object Checklist {
    fun render(part: Part): String = buildString {
        appendLine("# Checklist: ${part.name} (${part.id})")
        appendLine()
        appendLine("Tick a box when the value matches the source at that place.")
        appendLine("If something is wrong, say which line.")
        val all = items(part)
        part.sources.forEach { source ->
            val mine = all.filter { it.cite.source == source.id }
            if (mine.isNotEmpty()) appendSource(source, mine)
        }
    }

    private fun StringBuilder.appendSource(source: Source, items: List<Item>) {
        appendLine()
        appendLine("## ${source.id}: ${source.title}" + source.version?.let { " ($it)" }.orEmpty())
        appendLine()
        appendLine("<${source.url}>" + source.sha256?.let { " · SHA-256 `$it`" }.orEmpty())
        items.groupBy { place(it) }.toSortedMap(compareBy({ it.first }, { it.second })).forEach { (place, group) ->
            appendLine()
            appendLine("### ${place.second}")
            appendLine()
            group.sortedWith(compareBy({ it.file }, { it.line })).forEach {
                appendLine("- [ ] `${it.file}:${it.line}` ${it.text}")
            }
        }
    }

    // Pages first in number order, then sections, then refs.
    private fun place(item: Item): Pair<Int, String> {
        val cite = item.cite
        return when {
            cite.page != null -> cite.page to "Page ${cite.page}"
            cite.section != null -> Int.MAX_VALUE to "Section “${cite.section}”"
            else -> Int.MAX_VALUE to "Ref ${cite.ref}"
        }
    }
}
