package dev.partlore.tools.packer

import dev.partlore.core.packformat.PackFormat
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper

/** manifest.json: which packs exist, how big, and their SHA-256. A list, so packs can split by family later. */
object Manifest {
    data class Entry(
        val id: String,
        val version: String,
        val file: String,
        val size: Long,
        val sha256: String,
        val parts: Int,
        val preview: Boolean,
    )

    private val mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build()

    // Sorted maps: the same packs always give the same text.
    fun json(entries: List<Entry>): String = mapper.writeValueAsString(
        sortedMapOf(
            "schemaVersion" to PackFormat.SCHEMA_VERSION,
            "packs" to
                entries.map {
                    sortedMapOf(
                        "id" to it.id,
                        "version" to it.version,
                        "file" to it.file,
                        "size" to it.size,
                        "sha256" to it.sha256,
                        "parts" to it.parts,
                        "preview" to it.preview,
                    )
                },
        ),
    ) + "\n"
}
