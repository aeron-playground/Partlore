package dev.partlore.tools.content.load

import dev.partlore.tools.content.model.Citation
import dev.partlore.tools.content.model.Gotcha
import dev.partlore.tools.content.model.GotchaFile
import dev.partlore.tools.content.model.ImportedFrom
import dev.partlore.tools.content.model.Level
import dev.partlore.tools.content.model.Source
import dev.partlore.tools.content.model.Status
import tools.jackson.databind.JsonNode

internal fun mapStatus(node: JsonNode, file: String): Status = Status(
    level = Level.of(node.text("level")),
    checkedBy = node.texts("checked_by"),
    checkedOn = node.textOrNull("checked_on"),
    against = node.texts("against"),
    importedFrom = node.get("imported_from")?.let {
        ImportedFrom(it.text("importer"), it.text("source"), it.text("ref"), it.text("on"))
    },
    note = node.textOrNull("note"),
    pos = node.pos(file),
)

/** A citation is the source/page/section/ref fields of [node]; its position is the source value. */
internal fun mapCite(node: JsonNode, file: String): Citation = Citation(
    source = node.text("source"),
    page = node.intOrNull("page"),
    section = node.textOrNull("section"),
    ref = node.textOrNull("ref"),
    pos = (node.get("source") ?: node).pos(file),
)

internal fun mapSources(tree: JsonNode, file: String): List<Source> = tree.items("sources").map { s ->
    Source(
        id = s.text("id"),
        type = s.text("type"),
        title = s.text("title"),
        publisher = s.text("publisher"),
        url = s.text("url"),
        version = s.textOrNull("version"),
        retrieved = s.text("retrieved"),
        sha256 = s.textOrNull("sha256"),
        license = s.text("license"),
        pos = s.pos(file),
    )
}

internal fun mapGotchas(tree: JsonNode, file: String): GotchaFile = GotchaFile(
    file = file,
    status = mapStatus(tree.get("status"), file),
    gotchas =
    tree.items("gotchas").map { g ->
        Gotcha(
            id = g.text("id"),
            severity = g.text("severity"),
            title = g.text("title"),
            body = g.text("body"),
            pins = g.texts("pins"),
            cites = g.items("cites").map { mapCite(it, file) },
            pos = g.pos(file),
        )
    },
)
