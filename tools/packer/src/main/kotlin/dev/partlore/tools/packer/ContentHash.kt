package dev.partlore.tools.packer

import java.io.File
import java.security.MessageDigest

/** A fingerprint of the content: same files, same paths, same bytes → same hash, on any machine. */
internal object ContentHash {
    private val SKIPPED = setOf("README.md", "LICENSE")

    fun of(contentDir: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        contentDir.walkTopDown()
            .filter { it.isFile }
            .map { it.relativeTo(contentDir).invariantSeparatorsPath to it }
            .filterNot { (path, _) -> path in SKIPPED }
            .sortedBy { (path, _) -> path }
            .forEach { (path, file) ->
                digest.update(path.toByteArray())
                digest.update(0.toByte())
                digest.update(file.readBytes())
            }
        return digest.digest().toHex()
    }
}

internal fun sha256(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).toHex()

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
