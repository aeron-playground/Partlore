package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Scalar
import dev.partlore.tools.content.model.SpecKey
import dev.partlore.tools.content.model.SpecValue

/** A part's links to makers, categories, tags, other parts and spec keys. */
object PartLinkRules {
    // How to fix a spec value that doesn't fit its key's type.
    private val HOW =
        mapOf(
            "range" to "give min, typ or max, not value",
            "number" to "give one number as value",
            "text" to "give words as value",
            "bool" to "give true or false as value",
        )

    fun check(content: Content, d: Diagnostics) {
        val known = content.parts.map { it.id }.toSet() + content.unreadable
        val categories = content.categories.map { it.id }.toSet()
        val tags = content.tags.map { it.id }.toSet()
        val keys = content.specKeys.associateBy { it.key }
        content.parts.forEach { part ->
            maker(part, d)
            if (part.category !in categories) {
                d.error(part.at("category"), "category \"${part.category}\" is not in categories.yaml")
            }
            part.tags.filter { it !in tags }.forEach { d.error(part.at("tags"), "tag \"$it\" is not in tags.yaml") }
            links(part, known, d)
            (part.specs + part.absoluteMax).forEach { value ->
                specProblem(value, keys[value.key])?.let { d.error(value.pos, it) }
            }
            reportDuplicates(part.i2c, { it.text }, { it.pos }, "I²C address", d)
        }
    }

    private fun maker(part: Part, d: Diagnostics) {
        val maker = part.id.substringBefore('/')
        val expected = slug(part.manufacturer)
        if (maker != expected) {
            d.error(
                part.at("manufacturer"),
                "id starts with $maker/ but manufacturer \"${part.manufacturer}\" gives $expected/",
            )
        }
    }

    private fun links(part: Part, known: Set<String>, d: Diagnostics) {
        val targets = listOfNotNull(part.uses?.let { "uses" to it }) + part.related.map { "related" to it }
        targets.forEach { (field, target) ->
            when (target) {
                part.id -> d.error(part.at(field), "a part can't point to itself")
                !in known -> d.error(part.at(field), "part \"$target\" doesn't exist")
            }
        }
    }

    private fun specProblem(value: SpecValue, key: SpecKey?): String? = when {
        key == null -> "spec key \"${value.key}\" is not in spec-keys.yaml"

        value.unit != key.unit ->
            "${key.key} is measured in ${key.unit ?: "no unit"}, not ${value.unit ?: "no unit"}"

        else -> typeProblem(value, key)
    }

    private fun typeProblem(value: SpecValue, key: SpecKey): String? {
        val hasRange = value.min != null || value.typ != null || value.max != null
        val fits =
            when (key.type) {
                "range" -> value.value == null && hasRange
                "number" -> value.value is Scalar.Num && !hasRange
                "text" -> value.value is Scalar.Text && !hasRange
                else -> value.value is Scalar.Bool && !hasRange
            }
        return if (fits) null else "${key.key} is a ${key.type} key: ${HOW.getValue(key.type)}"
    }
}
