package dev.partlore.tools.packer

import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Scalar
import dev.partlore.tools.content.model.SpecKey
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.ship.Shipping

/** Everything about one shipped part, in a fixed order (the search index depends on insert order). */
internal fun Rows.part(part: Part, mode: Mode, keys: Map<String, SpecKey>) {
    insert("part", listOf(part.id, part.kind, part.name, part.manufacturer, part.category, part.summary, part.uses))
    status(part.id, "part", part.status)
    part.aliases.forEach { insert("part_alias", listOf(part.id, it)) }
    part.tags.forEach { insert("part_tag", listOf(part.id, it)) }
    part.related.forEach { insert("part_related", listOf(part.id, it)) }
    part.sources.forEach { s ->
        insert(
            "source",
            listOf(part.id, s.id, s.type, s.title, s.publisher, s.url, s.version, s.retrieved, s.sha256, s.license),
        )
    }
    specs(part, keys)
    part.i2c.forEach { a -> insert("i2c_address", listOf(part.id, a.address, a.isDefault, a.select) + cite(a.cite)) }
    if (Shipping.articleShips(part, mode)) insert("article", listOf(part.id, part.article))
    val pinsShip = Shipping.pinsShip(part, mode)
    val gotchasShip = Shipping.gotchasShip(part, mode)
    part.pins?.takeIf { pinsShip }?.let {
        status(part.id, "pins", it.status)
        pins(part.id, it)
    }
    part.gotchas?.takeIf { gotchasShip }?.let {
        status(part.id, "gotchas", it.status)
        gotchas(part.id, it)
    }
    val glance = Glance.of(part, pinsShip)
    insert(
        "glance",
        listOf(
            part.id,
            glance.logicLevelV,
            glance.fiveVTolerant,
            glance.gpioCount,
            glance.adcChannels,
            glance.wifi,
            glance.bluetooth,
        ),
    )
    searchRows(part, pinsShip, gotchasShip).forEach { insert("search", listOf(part.id, it.kind, it.ref, it.text)) }
}

private fun Rows.specs(part: Part, keys: Map<String, SpecKey>) {
    listOf(false to part.specs, true to part.absoluteMax).forEach { (absolute, values) ->
        values.forEachIndexed { seq, v ->
            val key = keys.getValue(v.key)
            val number = (v.value as? Scalar.Num)?.value
            val text =
                when (val value = v.value) {
                    is Scalar.Text -> value.value
                    is Scalar.Bool -> value.value.toString()
                    else -> null
                }
            insert(
                "spec",
                listOf(
                    part.id, absolute, seq, v.key, key.label, key.type, v.unit, v.min, v.typ, v.max, number, text,
                    v.condition,
                ) + cite(v.cite),
            )
        }
    }
}
