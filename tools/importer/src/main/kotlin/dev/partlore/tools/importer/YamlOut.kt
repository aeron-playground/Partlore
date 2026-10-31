package dev.partlore.tools.importer

import tools.jackson.databind.JsonNode

/** Writes YAML values the way the hand-written content files do: short maps and lists on one line. */
internal object YamlOut {
    // Plain words stay unquoted; anything YAML could read as a number, a date or a yes/no gets quotes.
    private val PLAIN = Regex("""^[A-Za-z_][A-Za-z0-9_.+/-]*$""")
    private val YAML_WORDS = setOf("true", "false", "null", "yes", "no", "on", "off", "y", "n")

    fun text(value: String): String {
        val plain = PLAIN.matches(value) && value.lowercase() !in YAML_WORDS
        return if (plain) value else quote(value)
    }

    fun quote(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""

    /** A value on one line: a scalar, `{ key: value, … }` or `[a, b]`. */
    fun inline(node: JsonNode): String = when {
        node.isObject && node.isEmpty -> "{}"
        node.isObject -> node.properties().joinToString(", ", "{ ", " }") { (key, value) -> "$key: ${inline(value)}" }
        node.isArray -> node.values().joinToString(", ", "[", "]") { inline(it) }
        node.isString -> text(node.asString())
        node.isNull -> "null"
        else -> node.asString()
    }
}
