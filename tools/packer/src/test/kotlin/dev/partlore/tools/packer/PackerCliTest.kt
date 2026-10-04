package dev.partlore.tools.packer

import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class PackerCliTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun packWritesThePackAndManifest() {
        val fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
        val out = File(tmp.root, "out")
        val output = ByteArrayOutputStream()
        val code =
            PackerCli(PrintStream(output, true, Charsets.UTF_8), false, TODAY)
                .run(arrayOf("pack", "--content", fixture.root.path, "--out", out.path))
        assertEquals(0, code)
        assertTrue(File(out, "manifest.json").isFile)
        assertTrue(output.toString(Charsets.UTF_8), output.toString(Charsets.UTF_8).contains("Packed 1 part into"))
    }

    @Test
    fun withoutAnOutFolderItShowsUsage() {
        val output = ByteArrayOutputStream()
        assertEquals(2, PackerCli(PrintStream(output, true, Charsets.UTF_8), false, TODAY).run(arrayOf("pack")))
    }
}
