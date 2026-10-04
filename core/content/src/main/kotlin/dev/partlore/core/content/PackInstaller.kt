package dev.partlore.core.content

import dev.partlore.core.model.ContentProblem
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.DigestInputStream
import java.security.MessageDigest

/** The pack inside the app: its manifest and a way to read a file next to it. */
class BundledPack(val readManifest: () -> String, val open: (fileName: String) -> InputStream)

sealed interface InstallResult {
    /** The pack to open; [replaced] is true when this call copied it. */
    data class Ready(val file: File, val replaced: Boolean) : InstallResult

    /** The bundled pack was bad; the previously installed one stays in use. */
    data class KeptPrevious(val file: File, val detail: String) : InstallResult

    data class Failed(val problem: ContentProblem, val detail: String) : InstallResult
}

/**
 * Copies the bundled pack into [dir] when it differs from the installed one. The copy goes to a
 * temporary file, is checked against the manifest's SHA-256 and is then renamed into place in one
 * step, so an interrupted copy never leaves a half-written pack behind.
 */
class PackInstaller(private val dir: File) {
    fun install(bundled: BundledPack, force: Boolean = false): InstallResult {
        val manifestText = readOrNull(bundled.readManifest)
        val wanted = manifestText?.let(PackManifest::parse)?.packs?.firstOrNull()
        return when {
            manifestText == null || wanted == null -> previousOr(
                ContentProblem.Damaged,
                "The bundled manifest can't be read.",
            )

            !force && File(dir, wanted.file).isFile && installed()?.sha256 == wanted.sha256 ->
                InstallResult.Ready(File(dir, wanted.file), replaced = false)

            else -> copy(bundled, wanted, manifestText)
        }
    }

    private fun copy(bundled: BundledPack, wanted: PackManifest.Pack, manifestText: String): InstallResult {
        dir.mkdirs()
        val target = File(dir, wanted.file)
        val temp = File(dir, wanted.file + TEMP)
        val sha = writeHashing(temp) { bundled.open(wanted.file) }
        return when {
            sha == null -> previousOr(ContentProblem.Missing, "The bundled pack ${wanted.file} can't be read.")

            sha != wanted.sha256 -> {
                temp.delete()
                previousOr(ContentProblem.Damaged, "The bundled pack ${wanted.file} failed its SHA-256 check.")
            }

            else -> {
                swapIn(temp, target, manifestText)
                InstallResult.Ready(target, replaced = true)
            }
        }
    }

    /** Copies [source] into [file], flushes it to disk and returns its SHA-256; null when it can't be read. */
    private fun writeHashing(file: File, source: () -> InputStream): String? = try {
        val digest = MessageDigest.getInstance("SHA-256")
        // Closing the DigestInputStream also closes the bundled stream inside it.
        DigestInputStream(source(), digest).use { hashed -> copyAndSync(hashed, file) }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (_: IOException) {
        file.delete()
        null
    }

    /** Writes [input] to [file] and waits until the bytes are on disk, so a power cut can't leave half a pack. */
    private fun copyAndSync(input: InputStream, file: File) = FileOutputStream(file).use { out ->
        input.copyTo(out)
        out.fd.sync()
    }

    private fun swapIn(temp: File, target: File, manifestText: String) {
        Files.move(temp.toPath(), target.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
        val manifestTemp = File(dir, MANIFEST + TEMP).apply { writeText(manifestText) }
        Files.move(manifestTemp.toPath(), File(dir, MANIFEST).toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
        dir.listFiles().orEmpty().filter { it.name != target.name && it.name != MANIFEST }.forEach { it.delete() }
    }

    private fun installed(): PackManifest.Pack? =
        File(dir, MANIFEST).takeIf { it.isFile }?.let { PackManifest.parse(it.readText()) }?.packs?.firstOrNull()

    private fun previousOr(problem: ContentProblem, detail: String): InstallResult {
        val previous = installed()?.let { File(dir, it.file) }?.takeIf { it.isFile }
        return if (previous !=
            null
        ) {
            InstallResult.KeptPrevious(previous, detail)
        } else {
            InstallResult.Failed(problem, detail)
        }
    }

    private fun readOrNull(read: () -> String): String? = try {
        read()
    } catch (_: IOException) {
        null
    }

    private companion object {
        const val MANIFEST = "manifest.json"
        const val TEMP = ".tmp"
    }
}
