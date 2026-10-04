package dev.partlore.tools.content.load

import com.networknt.schema.Schema
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.dialect.Dialects
import com.networknt.schema.serialization.NodeReader
import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Pos
import tools.jackson.databind.JsonNode
import java.io.File
import com.networknt.schema.Error as SchemaError

/**
 * Checks YAML trees against the JSON Schemas in content/schema. The schema files are handed over
 * as text under a made-up address on the reserved `.invalid` domain, so nothing is ever downloaded.
 */
class SchemaChecker(schemaDir: File, nodeReader: NodeReader) {
    private val registry: SchemaRegistry =
        SchemaRegistry.withDialect(Dialects.getDraft202012()) { builder ->
            builder.nodeReader(nodeReader).schemas(
                schemaDir.listFiles { file -> file.name.endsWith(".schema.json") }.orEmpty()
                    .associate { BASE + it.name to it.readText() },
            )
        }

    private val schemas = mutableMapOf<String, Schema>()

    /** Reports every schema problem in [tree]; true when there are none. */
    fun check(schemaFile: String, tree: JsonNode, file: String, diagnostics: Diagnostics): Boolean {
        val schema = schemas.getOrPut(schemaFile) { registry.getSchema(SchemaLocation.of(BASE + schemaFile)) }
        val errors = schema.validate(tree)
        errors.forEach { diagnostics.error(position(it, tree, file), describe(it)) }
        return errors.isEmpty()
    }

    // For an unknown key, point at the key itself, not at the block that holds it.
    private fun position(error: SchemaError, root: JsonNode, file: String): Pos {
        val node: JsonNode? = error.instanceNode
        val property: String? = error.property
        val target = if (error.keyword == "additionalProperties" && property != null) node?.get(property) else node
        return (target ?: node ?: root).pos(file)
    }

    private fun describe(error: SchemaError): String {
        val where = error.instanceLocation?.toString().orEmpty()
        return if (where.isEmpty() || where == "$" || where == "/") error.message else "$where: ${error.message}"
    }

    companion object {
        const val BASE: String = "https://partlore.invalid/schema/"
    }
}
