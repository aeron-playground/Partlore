package dev.partlore.tools.content.load

import dev.partlore.tools.content.diag.Diagnostic
import dev.partlore.tools.content.diag.Diagnostics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SchemaCheckerTest {
    @get:Rule val tmp = TemporaryFolder()

    private val yaml = YamlReader()

    private fun check(text: String): List<Diagnostic> {
        val dir = tmp.newFolder()
        File(dir, "thing.schema.json").writeText(THING)
        File(dir, "label.schema.json").writeText(LABEL)
        val d = Diagnostics()
        val tree = requireNotNull(yaml.read("thing.yaml", text, d))
        SchemaChecker(dir, yaml.nodeReader).check("thing.schema.json", tree, "thing.yaml", d)
        return d.all
    }

    @Test
    fun aValidFileHasNoProblems() {
        assertEquals(emptyList<Diagnostic>(), check("id: a\nvoltage: 3.3\nlabel: x\n"))
    }

    @Test
    fun anUnknownKeyIsReportedOnItsOwnLine() {
        val problem = check("id: a\nvoltage: 3.3\nlabel: x\nvotlage: 3.3\n").single()
        assertEquals(4, problem.line)
        assertTrue(problem.message, problem.message.contains("votlage"))
    }

    @Test
    fun aWrongTypeIsReportedOnTheValue() {
        val problem = check("id: a\nvoltage: \"3v3\"\nlabel: x\n").single()
        assertEquals(2, problem.line)
        assertTrue(problem.message, problem.message.contains("number"))
    }

    @Test
    fun aMissingKeyNamesTheKey() {
        val problem = check("id: a\nlabel: x\n").single()
        assertTrue(problem.message, problem.message.contains("voltage"))
    }

    @Test
    fun refsBetweenSchemaFilesResolveWithoutTheNetwork() {
        // label.schema.json only allows lowercase; this error proves the $ref was followed locally.
        val problem = check("id: a\nvoltage: 3.3\nlabel: X\n").single()
        assertEquals(3, problem.line)
    }

    private companion object {
        val THING =
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "additionalProperties": false,
              "required": ["id", "voltage", "label"],
              "properties": {
                "id": { "type": "string" },
                "voltage": { "type": "number" },
                "label": { "${'$'}ref": "label.schema.json" }
              }
            }
            """.trimIndent()
        val LABEL =
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "type": "string",
              "pattern": "^[a-z]+${'$'}"
            }
            """.trimIndent()
    }
}
