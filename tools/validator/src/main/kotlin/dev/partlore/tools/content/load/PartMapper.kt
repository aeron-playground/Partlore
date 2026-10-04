package dev.partlore.tools.content.load

import dev.partlore.tools.content.model.I2cAddress
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Scalar
import dev.partlore.tools.content.model.SpecValue
import tools.jackson.databind.JsonNode

private const val HEX = 16

internal fun mapPart(tree: JsonNode, folder: String, file: String, files: PartFiles): Part = Part(
    id = tree.text("id"),
    folder = folder,
    kind = tree.text("kind"),
    name = tree.text("name"),
    aliases = tree.texts("aliases"),
    manufacturer = tree.text("manufacturer"),
    category = tree.text("category"),
    tags = tree.texts("tags"),
    uses = tree.textOrNull("uses"),
    related = tree.texts("related"),
    summary = tree.text("summary"),
    specs = tree.items("specs").map { mapSpec(it, file) },
    absoluteMax = tree.items("absolute_max").map { mapSpec(it, file) },
    i2c = tree.items("i2c").map { mapI2c(it, file) },
    status = mapStatus(tree.get("status"), file),
    sources = files.sources,
    pins = files.pins,
    gotchas = files.gotchas,
    article = files.article,
    pos = tree.pos(file),
    fieldPos = tree.properties().associate { (key, value) -> key to value.pos(file) },
)

private fun mapSpec(node: JsonNode, file: String): SpecValue = SpecValue(
    key = node.text("key"),
    unit = node.textOrNull("unit"),
    min = node.doubleOrNull("min"),
    typ = node.doubleOrNull("typ"),
    max = node.doubleOrNull("max"),
    value = scalar(node.get("value")),
    condition = node.textOrNull("condition"),
    cite = mapCite(node, file),
    pos = node.pos(file),
)

private fun mapI2c(node: JsonNode, file: String): I2cAddress {
    val text = node.text("address")
    return I2cAddress(
        address = text.removePrefix("0x").toInt(HEX),
        text = text,
        isDefault = node.boolOrNull("default") ?: false,
        select = node.textOrNull("select"),
        cite = mapCite(node, file),
        pos = node.pos(file),
    )
}

private fun scalar(node: JsonNode?): Scalar? = when {
    node == null || node.isNull -> null
    node.isBoolean -> Scalar.Bool(node.booleanValue())
    node.isNumber -> Scalar.Num(node.doubleValue())
    else -> Scalar.Text(node.asString())
}
