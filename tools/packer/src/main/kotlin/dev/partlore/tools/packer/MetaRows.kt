package dev.partlore.tools.packer

import dev.partlore.core.packformat.PackFormat
import dev.partlore.tools.content.model.Citation
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Status
import dev.partlore.tools.content.ship.Mode

internal fun Rows.meta(content: Content, mode: Mode, contentSha256: String, parts: Int) {
    insert("meta", listOf("content_license", PackFormat.CONTENT_LICENSE))
    insert("meta", listOf("content_sha256", contentSha256))
    insert("meta", listOf("pack_version", content.version))
    insert("meta", listOf("part_count", parts.toString()))
    insert("meta", listOf("preview", if (mode == Mode.PREVIEW) "1" else "0"))
    insert("meta", listOf("schema_version", PackFormat.SCHEMA_VERSION.toString()))
}

internal fun Rows.shared(content: Content) {
    content.categories.forEach { insert("category", listOf(it.id, it.parent, it.name, it.sort)) }
    content.tags.forEach { insert("tag", listOf(it.id, it.label)) }
}

internal fun Rows.status(partId: String, file: String, status: Status) {
    val imported = status.importedFrom
    insert(
        "status",
        listOf(
            partId, file, status.level.yaml, status.checkedBy.joinToString(", ").ifEmpty { null }, status.checkedOn,
            status.against.joinToString(", ").ifEmpty { null }, imported?.importer, imported?.source, imported?.ref,
            imported?.on, status.note,
        ),
    )
}

/** source_id, page, section, ref: the four columns every citation takes. */
internal fun cite(c: Citation): List<Any?> = listOf(c.source, c.page, c.section, c.ref)
