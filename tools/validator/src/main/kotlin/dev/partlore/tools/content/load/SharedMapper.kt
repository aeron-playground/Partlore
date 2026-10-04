package dev.partlore.tools.content.load

import dev.partlore.tools.content.model.Category
import dev.partlore.tools.content.model.SpecKey
import dev.partlore.tools.content.model.Tag
import tools.jackson.databind.JsonNode

internal fun mapCategories(tree: JsonNode, file: String): List<Category> = tree.items("categories").map {
    Category(it.text("id"), it.text("name"), it.textOrNull("parent"), it.intOrNull("sort"), it.pos(file))
}

internal fun mapTags(tree: JsonNode, file: String): List<Tag> =
    tree.items("tags").map { Tag(it.text("id"), it.text("label"), it.pos(file)) }

internal fun mapSpecKeys(tree: JsonNode, file: String): List<SpecKey> = tree.items("keys").map {
    SpecKey(
        key = it.text("key"),
        label = it.text("label"),
        type = it.text("type"),
        unit = it.textOrNull("unit"),
        description = it.text("description"),
        pos = it.pos(file),
    )
}
