package dev.partlore.tools.importer

import dev.partlore.tools.importer.rst.PinTable
import tools.jackson.core.JacksonException
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonNode
import tools.jackson.dataformat.yaml.YAMLMapper

/**
 * What a person adds for one board, each fact with its own source: the part's name, where the
 * headers sit and how safe each pin is. The importer copies these blocks; it never makes them up.
 */
class Hints private constructor(
    val part: JsonNode,
    val guide: GuideHint,
    val sources: List<JsonNode>,
    val headers: List<HeaderHint>,
    val pins: List<PinHint>,
) {
    data class GuideHint(val ref: String, val url: String, val version: String)

    data class HeaderHint(val table: String, val node: JsonNode)

    data class PinHint(val table: String, val no: Int, val node: JsonNode)

    fun pin(table: String, no: Int): JsonNode = pins.first { it.table == table && it.no == no }.node

    /**
     * Every guide table needs a header hint and every pin exactly one pin hint.
     * A hint the guide can't match is a mistake too.
     */
    fun check(tables: List<PinTable>) {
        checkHeaders(tables)
        checkEveryPinOnce(tables)
        pins.firstOrNull { p -> tables.none { t -> t.heading == p.table && t.rows.any { it.no == p.no } } }?.let {
            throw ImportException("${it.table} No. ${it.no} is in the hints but not in the guide")
        }
    }

    private fun checkHeaders(tables: List<PinTable>) {
        tables.firstOrNull { t -> headers.none { it.table == t.heading } }?.let {
            throw ImportException("table ${it.heading} has no header in the hints")
        }
        headers.firstOrNull { h -> tables.none { it.heading == h.table } }?.let {
            throw ImportException("header ${it.table} in the hints is not a table in the guide")
        }
    }

    private fun checkEveryPinOnce(tables: List<PinTable>) {
        val hinted = pins.groupBy { it.table to it.no }
        hinted.entries.firstOrNull { it.value.size > 1 }?.let { (pin) ->
            throw ImportException("${pin.first} No. ${pin.second} is in the hints twice")
        }
        tables.firstNotNullOfOrNull { t ->
            t.rows.firstOrNull { (t.heading to it.no) !in hinted }?.let {
                t.heading to
                    it.no
            }
        }
            ?.let { (table, no) -> throw ImportException("$table No. $no has no hint: every pin needs `safe`") }
    }

    companion object {
        private val ROOT_KEYS = setOf("part", "guide", "sources", "headers", "pins")
        private val PART_KEYS = setOf("id", "name", "aliases", "category", "tags", "summary")
        private val GUIDE_KEYS = setOf("ref", "url", "version")
        private val HEADER_KEYS =
            setOf("table", "id", "label", "type", "pitch_mm", "source", "page", "section", "ref", "edges")
        private val PIN_KEYS =
            setOf(
                "table", "no", "safe", "cites", "board_label", "reserved", "strapping", "voltage",
                "five_v_tolerant", "note", "module_pad", "arduino", "label_aliases",
            )

        // A hint that says more than "an ordinary pin" is a judgement, and a judgement needs a source.
        private val JUDGEMENTS = PIN_KEYS - setOf("table", "no", "safe", "cites")

        // A key written twice is almost always a typo; Jackson would silently keep the last one.
        private val mapper = YAMLMapper.builder().enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).build()

        fun read(yaml: String): Hints {
            val root =
                try {
                    mapper.readTree(yaml)
                } catch (e: JacksonException) {
                    throw ImportException("hints can't be read: ${e.message}", e)
                }
            keys(root, "hints", ROOT_KEYS, ROOT_KEYS - "sources")
            val part = root.get("part")
            keys(part, "part", PART_KEYS, setOf("id", "name", "category", "summary"))
            val guide = root.get("guide")
            keys(guide, "guide", GUIDE_KEYS, GUIDE_KEYS)
            val headers =
                list(root, "headers").mapIndexed { i, header ->
                    keys(header, "headers[$i]", HEADER_KEYS, HEADER_KEYS - setOf("page", "section", "ref"))
                    HeaderHint(header.get("table").asString(), header)
                }
            return Hints(
                part = part,
                guide = GuideHint(
                    guide.get("ref").asString(),
                    guide.get("url").asString(),
                    guide.get("version").asString(),
                ),
                sources = if (root.has("sources")) list(root, "sources") else emptyList(),
                headers = headers,
                pins = list(root, "pins").mapIndexed(::pin),
            )
        }

        private fun pin(i: Int, node: JsonNode): PinHint {
            keys(node, "pins[$i]", PIN_KEYS, setOf("table", "no", "safe"))
            if (!node.get("no").isInt) throw ImportException("pins[$i]: `no` must be a pin number")
            val hint = PinHint(node.get("table").asString(), node.get("no").intValue(), node)
            val judged = node.get("safe").asString() != "ok" || JUDGEMENTS.any { node.has(it) }
            if (judged && !node.has("cites")) {
                throw ImportException(
                    "${hint.table} No. ${hint.no}: this hint needs `cites` (where the fact comes from)",
                )
            }
            return hint
        }

        private fun list(node: JsonNode, name: String): List<JsonNode> {
            val items = node.get(name)
            if (items == null || !items.isArray) throw ImportException("`$name` must be a list")
            return items.values().toList()
        }

        private fun keys(node: JsonNode?, where: String, allowed: Set<String>, required: Set<String>) {
            val names = map(node, where).properties().map { it.key }
            names.firstOrNull { it !in allowed }?.let { throw ImportException("$where: unknown key '$it'") }
            required.firstOrNull { it !in names }?.let { throw ImportException("$where: missing '$it'") }
        }

        private fun map(node: JsonNode?, where: String): JsonNode {
            if (node == null || !node.isObject) throw ImportException("$where must be a map")
            return node
        }
    }
}
