package dev.partlore.tools.packer

import dev.partlore.tools.content.model.Part

internal data class SearchRow(val kind: String, val ref: String, val text: String)

/** What search can find for a part: name, maker, aliases, every pin name, gotcha titles (shipped data only). */
internal fun searchRows(part: Part, pinsShip: Boolean, gotchasShip: Boolean): List<SearchRow> = buildList {
    add(SearchRow("part", "", part.name))
    add(SearchRow("part", "", part.manufacturer))
    part.aliases.forEach { add(SearchRow("alias", "", it)) }
    if (pinsShip) part.pins?.pins?.forEach { pin -> pin.names.forEach { add(SearchRow("pin", pin.id, it)) } }
    if (gotchasShip) part.gotchas?.gotchas?.forEach { add(SearchRow("gotcha", it.id, it.title)) }
}
