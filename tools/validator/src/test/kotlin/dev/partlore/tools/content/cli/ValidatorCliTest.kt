package dev.partlore.tools.content.cli

import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import dev.partlore.tools.content.testing.TODAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class ValidatorCliTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture
    private val output = ByteArrayOutputStream()

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    private fun run(github: Boolean, vararg args: String): Int =
        ValidatorCli(PrintStream(output, true, Charsets.UTF_8), github, TODAY).run(arrayOf(*args))

    private val text get() = output.toString(Charsets.UTF_8)

    @Test
    fun validContentExitsZero() {
        assertEquals(0, run(false, "validate", "--content", fixture.root.path))
        assertTrue(text, text.contains("1 part, 0 errors"))
    }

    @Test
    fun problemsExitOneWithFileLineAndColumn() {
        fixture.edit("$BOARD_DIR/part.yaml") { it.replace("source: s2, section", "source: s9, section") }
        assertEquals(1, run(false, "validate", "--content", fixture.root.path))
        assertTrue(text, text.contains("${fixture.root.path}/$BOARD_DIR/part.yaml:"))
        assertTrue(text, text.contains(": error: source \"s9\""))
    }

    @Test
    fun onGitHubEachProblemIsAlsoAnAnnotation() {
        fixture.edit("$BOARD_DIR/part.yaml") { it.replace("source: s2, section", "source: s9, section") }
        run(true, "validate", "--content", fixture.root.path)
        assertTrue(text, text.contains("::error file=${fixture.root.path}/$BOARD_DIR/part.yaml,line="))
    }

    @Test
    fun checklistWritesOneFilePerPart() {
        val out = tmp.newFolder("checklists")
        assertEquals(0, run(false, "checklist", "--content", fixture.root.path, "--out", out.path))
        assertTrue(File(out, "testmaker/test-board.md").readText().startsWith("# Checklist: Test Board"))
    }

    @Test
    fun aWrongCommandShowsUsage() {
        assertEquals(2, run(false, "frobnicate"))
        assertTrue(text, text.startsWith("usage:"))
    }
}
