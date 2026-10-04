package dev.partlore.tools.content.load

import com.networknt.schema.InputFormat
import com.networknt.schema.serialization.NodeReader
import com.networknt.schema.utils.JsonNodes
import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Pos
import tools.jackson.core.JacksonException
import tools.jackson.core.TokenStreamLocation
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonNode
import tools.jackson.dataformat.yaml.YAMLMapper

/** Reads YAML into trees that remember the line and column of every value. */
class YamlReader {
    val nodeReader: NodeReader =
        NodeReader.builder()
            // A key written twice is almost always a typo; Jackson would silently keep the last one.
            .yamlMapper(YAMLMapper.builder().enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).build())
            .locationAware()
            .build()

    /** The tree, or null after reporting why the file can't be read. */
    fun read(file: String, text: String, diagnostics: Diagnostics): JsonNode? = try {
        val tree: JsonNode? = nodeReader.readTree(text.removePrefix(BYTE_ORDER_MARK), InputFormat.YAML)
        if (tree == null || tree.isMissingNode || tree.isNull) {
            diagnostics.error(Pos(file, 1, 1), "file is empty")
            null
        } else {
            tree
        }
    } catch (e: JacksonException) {
        diagnostics.error(e.location.toPos(file), e.originalMessage ?: "not valid YAML")
        null
    }

    private companion object {
        const val BYTE_ORDER_MARK = "\uFEFF"
    }
}

/** Where this value starts in [file]. */
fun JsonNode.pos(file: String): Pos = JsonNodes.tokenStreamLocationOf(this).toPos(file)

private fun TokenStreamLocation?.toPos(file: String): Pos =
    Pos(file, this?.lineNr?.coerceAtLeast(1) ?: 1, this?.columnNr?.coerceAtLeast(1) ?: 1)
