package dev.partlore.tools.content.load

import dev.partlore.tools.content.diag.Diagnostics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YamlReaderTest {
    private val reader = YamlReader()

    @Test
    fun everyValueKnowsItsLine() {
        val tree = checkNotNull(reader.read("f.yaml", "id: a\nvoltage: 3.3\n", Diagnostics()))
        assertEquals(2, tree.get("voltage").pos("f.yaml").line)
    }

    @Test
    fun aDuplicateKeyIsAnErrorOnItsLine() {
        val d = Diagnostics()
        assertNull(reader.read("f.yaml", "id: a\nvoltage: 1\nvoltage: 2\n", d))
        val problem = d.all.single()
        assertEquals(3, problem.line)
        assertTrue(problem.message, problem.message.contains("voltage"))
    }

    @Test
    fun brokenYamlIsAnErrorNotACrash() {
        val d = Diagnostics()
        assertNull(reader.read("f.yaml", "id: a\nlabel: [x\n", d))
        assertTrue(d.hasErrors)
    }

    @Test
    fun anEmptyFileIsAnError() {
        val d = Diagnostics()
        assertNull(reader.read("f.yaml", "", d))
        assertEquals("file is empty", d.all.single().message)
    }

    @Test
    fun aByteOrderMarkIsIgnored() {
        val tree = reader.read("f.yaml", "\uFEFFid: a\nvoltage: 3.3\n", Diagnostics())
        assertEquals("a", tree!!.get("id").asString())
        assertEquals(2, tree.get("voltage").pos("f.yaml").line)
    }

    @Test
    fun windowsLineEndingsKeepLineNumbers() {
        val tree = reader.read("f.yaml", "id: a\r\nlabel: b\r\nvoltage: 3.3\r\n", Diagnostics())
        assertEquals(3, tree!!.get("voltage").pos("f.yaml").line)
    }
}
