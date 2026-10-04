package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Pos
import dev.partlore.tools.content.model.Citation
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Source

/** Every citation points at an existing source, at the right kind of place. */
object CitationRules {
    fun check(content: Content, d: Diagnostics) = content.parts.forEach { check(it, d) }

    private fun check(part: Part, d: Diagnostics) {
        reportDuplicates(part.sources, { it.id }, { it.pos }, "source", d)
        val sources = part.sourcesById
        part.citations.forEach { cite ->
            val source = sources[cite.source]
            if (source == null) {
                d.error(cite.pos, "source \"${cite.source}\" is not in sources.yaml")
            } else {
                placeProblem(source, cite)?.let { d.error(cite.pos, it) }
            }
        }
        statusSources(part).filter { (id, _) -> id !in sources }.forEach { (id, pos) ->
            d.error(pos, "status: source \"$id\" is not in sources.yaml")
        }
    }

    private fun placeProblem(source: Source, cite: Citation): String? = when {
        source.type in PDF_TYPES && cite.page == null -> "source \"${source.id}\" is a ${source.type}: cite a page"
        source.type == "web-doc" && cite.section == null -> "source \"${source.id}\" is a web page: cite a section"
        source.type == "dataset" && cite.ref == null -> "source \"${source.id}\" is a dataset: cite a ref"
        source.type != "dataset" && cite.ref != null -> "ref is only for dataset sources"
        source.type !in PDF_TYPES && cite.page != null -> "page is only for PDF sources"
        else -> null
    }

    private fun statusSources(part: Part): List<Pair<String, Pos>> =
        listOfNotNull(part.status, part.pins?.status, part.gotchas?.status).flatMap { status ->
            val imported = listOfNotNull(status.importedFrom?.let { it.source to status.pos })
            status.against.map { it to status.pos } + imported
        }
}
