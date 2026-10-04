package dev.partlore.tools.content.load

import com.networknt.schema.InputFormat
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.dialect.Dialects
import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.io.File

class SchemaFilesTest {
    private val schemaFiles =
        File(System.getProperty(ContentFixture.SCHEMA_DIR_PROPERTY)).listFiles { f -> f.name.endsWith(".schema.json") }
            .orEmpty().sortedBy { it.name }

    @Test
    fun thereAreNineSchemaFiles() {
        assertEquals(9, schemaFiles.size)
    }

    @Test
    fun everySchemaIsValidJsonSchema202012() {
        val dialect = Dialects.getDraft202012()
        val meta = SchemaRegistry.withDialect(dialect).getSchema(SchemaLocation.of(dialect.id))
        schemaFiles.forEach { file ->
            assertEquals(file.name, emptyList<Any>(), meta.validate(file.readText(), InputFormat.JSON))
        }
    }

    @Test
    fun everyPropertyHasADescription() {
        val mapper = JsonMapper.builder().build()
        schemaFiles.forEach { file ->
            val missing = mutableListOf<String>()
            collectMissing(mapper.readTree(file), "", missing)
            assertTrue("${file.name}: no description for $missing", missing.isEmpty())
        }
    }

    private fun collectMissing(node: JsonNode, path: String, missing: MutableList<String>) {
        if (node.isObject) {
            node.get("properties")?.properties()?.forEach { (name, schema) ->
                if (schema.get("description") == null) missing += "$path/$name"
            }
            node.properties().forEach { (name, child) -> collectMissing(child, "$path/$name", missing) }
        } else if (node.isArray) {
            node.values().forEach { collectMissing(it, path, missing) }
        }
    }
}
