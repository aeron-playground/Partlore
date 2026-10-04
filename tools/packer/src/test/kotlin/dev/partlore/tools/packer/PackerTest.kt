package dev.partlore.tools.packer

import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PackerTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun content(vararg boards: String) =
        ContentFixture(tmp.newFolder()).apply { boards.forEach { addBoard(it) } }

    private fun pack(fixture: ContentFixture, mode: Mode = Mode.RELEASE): Packer.Result =
        Packer(fixture.root, tmp.newFolder(), TODAY).pack(mode)

    private fun packed(fixture: ContentFixture): Packer.Result.Packed = pack(fixture) as Packer.Result.Packed

    @Test
    fun theSameContentGivesTheSameBytes() {
        val fixture = content("testmaker/a-board")
        assertEquals(packed(fixture).entry.sha256, packed(fixture).entry.sha256)
    }

    @Test
    fun packsAreIdenticalWhateverOrderFoldersWereCreatedIn() {
        val first = packed(content("testmaker/a-board", "testmaker/b-board"))
        val second = packed(content("testmaker/b-board", "testmaker/a-board"))
        assertEquals(first.entry.sha256, second.entry.sha256)
    }

    @Test
    fun theManifestDescribesThePackFile() {
        val result = packed(content("testmaker/a-board"))
        val manifest = File(result.file.parentFile, "manifest.json").readText()
        assertEquals(sha256(result.file), result.entry.sha256)
        assertTrue(manifest, manifest.contains("\"sha256\" : \"${result.entry.sha256}\""))
        assertTrue(manifest, manifest.contains("\"size\" : ${result.file.length()}"))
        assertTrue(manifest, manifest.contains("\"schemaVersion\" : 1"))
        assertEquals("partlore-content-0.0.1.db", result.file.name)
    }

    @Test
    fun invalidContentIsNotPacked() {
        val fixture = content("testmaker/a-board")
        fixture.edit("parts/testmaker/a-board/part.yaml") { it.replace("source: s2, section", "source: s9, section") }
        assertTrue(pack(fixture) is Packer.Result.Invalid)
    }

    @Test
    fun anEmptyContentFolderPacksAnEmptyPack() {
        val result = packed(content())
        assertEquals(0, result.entry.parts)
        assertEquals("0", single(result.file, "SELECT count(*) FROM part"))
    }

    @Test
    fun previewPacksSayTheyArePreviews() {
        val fixture = content("testmaker/a-board")
        fixture.edit("parts/testmaker/a-board/part.yaml") { it.replace("level: checked", "level: draft") }
        val result = pack(fixture, Mode.PREVIEW) as Packer.Result.Packed
        assertTrue(result.entry.preview)
        assertEquals("1", single(result.file, "SELECT value FROM meta WHERE key = 'preview'"))
        assertEquals("testmaker/a-board", single(result.file, "SELECT id FROM part"))
    }
}
