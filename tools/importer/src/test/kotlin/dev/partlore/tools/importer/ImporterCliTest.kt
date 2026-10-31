package dev.partlore.tools.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class ImporterCliTest {
    @get:Rule val folder = TemporaryFolder()

    private lateinit var checkout: File
    private lateinit var hints: File
    private lateinit var out: File
    private val printed = ByteArrayOutputStream()
    private val errors = ByteArrayOutputStream()

    @Before
    fun setUp() {
        checkout = folder.newFolder("esp-dev-kits")
        File(checkout, "docs/en/board").mkdirs()
        File(checkout, "docs/en/board/user_guide.rst").writeText(ImporterTest.GUIDE)
        git("init", "-q")
        git("add", ".")
        git("-c", "user.name=t", "-c", "user.email=t@example.com", "commit", "-q", "-m", "guide")
        hints = folder.newFile("board.yaml").apply { writeText(ImporterTest.HINTS) }
        out = folder.newFolder("parts")
    }

    private fun git(vararg args: String) {
        val process = ProcessBuilder("git", *args).directory(checkout).redirectErrorStream(true).start()
        check(process.waitFor() == 0) { process.inputStream.bufferedReader().readText() }
    }

    private fun run(vararg args: String): Int =
        ImporterCli(PrintStream(printed), PrintStream(errors)).run(arrayOf(*args))

    private fun importBoard(): Int =
        run("--checkout", checkout.path, "--hints", hints.path, "--out", out.path, "--on", "2026-10-31")

    private fun head(): String = ProcessBuilder(
        "git",
        "rev-parse",
        "HEAD",
    ).directory(checkout).start().inputStream.bufferedReader().readText().trim()

    @Test
    fun writesTheThreeFilesAndPrintsWhatToCheck() {
        assertEquals(errors.toString(), 0, importBoard())
        val dir = File(out, "example/board")
        assertEquals(listOf("part.yaml", "pins.yaml", "sources.yaml"), dir.list()!!.sorted())
        assertTrue(File(dir, "sources.yaml").readText().contains(head()))
        assertTrue(printed.toString(), printed.toString().contains("footnote 2"))
    }

    @Test
    fun importingAgainOverImportedFilesIsFine() {
        assertEquals(0, importBoard())
        assertEquals(errors.toString(), 0, importBoard())
    }

    @Test
    fun checkedFilesAreNeverOverwritten() {
        val pins = File(out, "example/board/pins.yaml").apply { parentFile.mkdirs() }
        pins.writeText("status:\n  level: checked\n")
        assertEquals(1, importBoard())
        assertEquals("status:\n  level: checked\n", pins.readText())
        assertTrue(errors.toString(), errors.toString().contains("pins.yaml"))
    }

    @Test
    fun markedFilesStayUnlessTheyAreStillImported() {
        assertEquals(0, importBoard())
        val pins = File(out, "example/board/pins.yaml")
        val checked = pins.readText().replace("level: imported", "level: checked")
        pins.writeText(checked)
        assertEquals(1, importBoard())
        assertEquals(checked, pins.readText())
        val noStatus = checked.substringBefore("status:")
        pins.writeText(noStatus)
        assertEquals(1, importBoard())
        assertEquals(noStatus, pins.readText())
    }

    @Test
    fun aGuideWithLocalChangesStops() {
        File(checkout, "docs/en/board/user_guide.rst").appendText("\nA local edit.\n")
        assertEquals(1, importBoard())
        assertTrue(errors.toString(), errors.toString().contains("local changes"))
    }

    @Test
    fun aMissingOptionIsAUsageError() {
        assertEquals(2, run("--checkout", checkout.path))
    }
}
