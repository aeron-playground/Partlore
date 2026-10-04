package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content

/** categories.yaml, tags.yaml and spec-keys.yaml. */
object SharedFileRules {
    private val UNITLESS_TYPES = setOf("text", "bool")

    fun check(content: Content, d: Diagnostics) {
        reportDuplicates(content.categories, { it.id }, { it.pos }, "category", d)
        reportDuplicates(content.tags, { it.id }, { it.pos }, "tag", d)
        reportDuplicates(content.specKeys, { it.key }, { it.pos }, "spec key", d)
        val categories = content.categories.map { it.id }.toSet()
        content.categories.filter { it.parent != null && it.parent !in categories }.forEach {
            d.error(it.pos, "category ${it.id}: parent \"${it.parent}\" is not in categories.yaml")
        }
        content.specKeys.filter { it.type in UNITLESS_TYPES && it.unit != null }.forEach {
            d.error(it.pos, "spec key ${it.key} is ${it.type}, so it can't have a unit")
        }
    }
}
