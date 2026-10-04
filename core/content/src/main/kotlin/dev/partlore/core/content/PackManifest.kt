package dev.partlore.core.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The parts of manifest.json the app uses. The packer writes it (tools/packer, Manifest.kt). */
@Serializable
internal data class PackManifest(val schemaVersion: Int, val packs: List<Pack>) {
    @Serializable
    data class Pack(val id: String, val version: String, val file: String, val sha256: String, val preview: Boolean)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** The manifest, or null when [text] isn't one. */
        fun parse(text: String): PackManifest? = try {
            json.decodeFromString(serializer(), text)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
