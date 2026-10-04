package dev.partlore.core.content

import dev.partlore.core.model.ContentProblem
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class PackInstallerTest {
    @get:Rule val tmp = TemporaryFolder()

    private val dir by lazy { tmp.newFolder("installed") }

    /** A real pack and manifest from the packer; [version] changes the file name. */
    private fun bundled(version: String = "0.0.1"): Pair<BundledPack, File> {
        val pack = buildPack(tmp) {
            addBoard()
            write("version.txt", "$version\n")
        }
        val manifest = File(pack.parentFile, "manifest.json").readText()
        return BundledPack({ manifest }, { name -> File(pack.parentFile, name).inputStream() }) to pack
    }

    @Test
    fun firstInstallCopiesThePackAndItsManifest() {
        val (pack, source) = bundled()
        val result = PackInstaller(dir).install(pack)
        assertTrue(result is InstallResult.Ready && result.replaced)
        assertArrayEquals(source.readBytes(), File(dir, source.name).readBytes())
        assertTrue(File(dir, "manifest.json").isFile)
    }

    @Test
    fun theSamePackIsNotCopiedAgain() {
        val (pack, _) = bundled()
        PackInstaller(dir).install(pack)
        var opens = 0
        val counting = BundledPack(pack.readManifest) { name ->
            opens++
            pack.open(name)
        }
        val result = PackInstaller(dir).install(counting)
        assertTrue(result is InstallResult.Ready && !result.replaced)
        assertEquals(0, opens)
    }

    @Test
    fun aNewBundledPackReplacesTheOldOne() {
        val (old, oldFile) = bundled("0.0.1")
        val (new, newFile) = bundled("0.0.2")
        PackInstaller(dir).install(old)
        val result = PackInstaller(dir).install(new)
        assertEquals(InstallResult.Ready(File(dir, newFile.name), replaced = true), result)
        assertFalse(File(dir, oldFile.name).exists())
    }

    @Test
    fun aBadCopyKeepsThePreviousPack() {
        val (old, oldFile) = bundled("0.0.1")
        val (new, _) = bundled("0.0.2")
        PackInstaller(dir).install(old)
        val corrupt = BundledPack(new.readManifest) { ByteArrayInputStream(byteArrayOf(1, 2, 3)) }
        val result = PackInstaller(dir).install(corrupt)
        assertTrue("got $result", result is InstallResult.KeptPrevious && result.file == File(dir, oldFile.name))
    }

    @Test
    fun aBadPackWithNothingInstalledFails() {
        val (pack, _) = bundled()
        val corrupt = BundledPack(pack.readManifest) { ByteArrayInputStream(byteArrayOf(1, 2, 3)) }
        val result = PackInstaller(dir).install(corrupt)
        assertTrue(result is InstallResult.Failed && result.problem == ContentProblem.Damaged)
    }

    @Test
    fun aMissingBundledFileFails() {
        val (pack, _) = bundled()
        val missing = BundledPack(pack.readManifest) { name -> throw FileNotFoundException(name) }
        val result = PackInstaller(dir).install(missing)
        assertTrue(result is InstallResult.Failed && result.problem == ContentProblem.Missing)
    }

    @Test
    fun anUnreadableManifestFails() {
        val result = PackInstaller(dir).install(BundledPack({ "not json" }) { InputStream.nullInputStream() })
        assertTrue(result is InstallResult.Failed && result.problem == ContentProblem.Damaged)
    }

    @Test
    fun forceCopiesAgain() {
        val (pack, source) = bundled()
        PackInstaller(dir).install(pack)
        File(dir, source.name).writeText("damaged")
        val result = PackInstaller(dir).install(pack, force = true)
        assertTrue(result is InstallResult.Ready && result.replaced)
        assertArrayEquals(source.readBytes(), File(dir, source.name).readBytes())
    }

    @Test
    fun leftoverTempFilesAreRemoved() {
        val (pack, _) = bundled()
        File(dir, "partlore-content-0.0.0.db.tmp").writeText("half a copy")
        PackInstaller(dir).install(pack)
        assertTrue(dir.listFiles().orEmpty().none { it.name.endsWith(".tmp") })
    }

    @Test
    fun theManifestTheAppReadsIsTheOneThePackerWrites() {
        val (pack, source) = bundled()
        val manifest = checkNotNull(PackManifest.parse(pack.readManifest()))
        assertEquals(source.name, manifest.packs.single().file)
        assertFalse(manifest.packs.single().preview)
    }
}
