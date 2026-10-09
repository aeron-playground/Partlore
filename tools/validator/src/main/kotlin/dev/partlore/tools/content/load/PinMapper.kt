package dev.partlore.tools.content.load

import dev.partlore.tools.content.model.Header
import dev.partlore.tools.content.model.HeaderRun
import dev.partlore.tools.content.model.Pin
import dev.partlore.tools.content.model.PinFile
import dev.partlore.tools.content.model.PinFunction
import dev.partlore.tools.content.model.Strapping
import tools.jackson.databind.JsonNode

internal fun mapPins(tree: JsonNode, file: String): PinFile = PinFile(
    file = file,
    status = mapStatus(tree.get("status"), file),
    headers = tree.items("headers").map { mapHeader(it, file) },
    pins = tree.items("pins").map { mapPin(it, file) },
)

private fun mapHeader(node: JsonNode, file: String): Header = Header(
    id = node.text("id"),
    label = node.text("label"),
    type = node.text("type"),
    rows = node.int("rows"),
    pinsPerRow = node.int("pins_per_row"),
    pitchMm = node.get("pitch_mm").doubleValue(),
    cite = mapCite(node, file),
    runs = node.items("edges").map { mapRun(it, file) },
    pos = node.pos(file),
)

private fun mapRun(node: JsonNode, file: String): HeaderRun = HeaderRun(
    edge = node.text("edge"),
    run = node.textOrNull("run"),
    pins = node.int("pins"),
    first = node.text("first"),
    row1 = node.textOrNull("row1"),
    order = node.intOrNull("order"),
    cite = mapCite(node, file),
    pos = node.pos(file),
)

private fun mapPin(node: JsonNode, file: String): Pin = Pin(
    id = node.text("id"),
    header = node.text("header"),
    row = node.int("row"),
    index = node.int("index"),
    chipName = node.textOrNull("chip_name"),
    modulePad = node.textOrNull("module_pad"),
    boardLabel = node.textOrNull("board_label"),
    labelAliases = node.texts("label_aliases"),
    arduino = node.textOrNull("arduino"),
    direction = node.text("direction"),
    voltage = node.doubleOrNull("voltage"),
    fiveVTolerant = node.boolOrNull("five_v_tolerant"),
    functions =
    node.items("functions").map {
        PinFunction(it.text("type"), it.textOrNull("signal"), it.boolOrNull("default") ?: false)
    },
    strapping =
    node.get("strapping")?.takeUnless { it.isNull }?.let {
        Strapping(it.text("role"), it.textOrNull("must_be"), it.text("at"))
    },
    safe = node.text("safe"),
    note = node.textOrNull("note"),
    cites = node.items("cites").map { mapCite(it, file) },
    pos = node.pos(file),
)
