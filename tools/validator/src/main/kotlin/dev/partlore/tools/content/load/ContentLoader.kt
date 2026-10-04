package dev.partlore.tools.content.load

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Pos
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Part
import tools.jackson.databind.JsonNode
import java.io.File

/** Reads content/ into the model. A part with any unreadable file is reported and left out. */
class ContentLoader(private val contentDir: File) {
    private val yaml = YamlReader()
    private val schemas = SchemaChecker(File(contentDir, "schema"), yaml.nodeReader)

    fun load(diagnostics: Diagnostics): Content {
        val parts = partFolders(diagnostics).associateWith { loadPart(it, diagnostics) }
        return Content(
            version = version(diagnostics),
            categories = shared("categories.yaml", "categories.schema.json", diagnostics, ::mapCategories),
            tags = shared("tags.yaml", "tags.schema.json", diagnostics, ::mapTags),
            specKeys = shared("schema/spec-keys.yaml", "spec-keys.schema.json", diagnostics, ::mapSpecKeys),
            parts = parts.values.filterNotNull().sortedBy { it.id },
            unreadable = parts.filterValues { it == null }.keys,
        )
    }

    private fun version(d: Diagnostics): String {
        val file = File(contentDir, "version.txt")
        val text = if (file.isFile) file.readText().trim() else ""
        if (!VERSION.matches(text)) d.error(Pos("version.txt", 1, 1), "version.txt must hold a version like 1.2.3")
        return text
    }

    private fun <T> shared(path: String, schema: String, d: Diagnostics, map: (JsonNode, String) -> List<T>): List<T> =
        (read(path, schema, d, required = true) as? Read.Ok)?.let { map(it.tree, path) }.orEmpty()

    // Folder listings are sorted: the file system's order differs between machines.
    private fun partFolders(d: Diagnostics): List<String> {
        val makers = File(contentDir, "parts").listFiles().orEmpty().sortedBy { it.name }
        makers.filter { it.isFile }.forEach { unexpected("parts/${it.name}", "parts/ holds one folder per maker", d) }
        return makers.filter { it.isDirectory }.flatMap { maker ->
            val entries = maker.listFiles().orEmpty().sortedBy { it.name }
            entries.filter { it.isFile }.forEach {
                unexpected("parts/${maker.name}/${it.name}", "a maker folder holds one folder per part", d)
            }
            entries.filter { it.isDirectory }.map { "${maker.name}/${it.name}" }
        }
    }

    private fun loadPart(folder: String, d: Diagnostics): Part? {
        val base = "parts/$folder"
        File(contentDir, base).listFiles().orEmpty().sortedBy { it.name }.filterNot { it.name in PART_FILES }
            .forEach { unexpected("$base/${it.name}", "a part folder holds ${PART_FILES.joinToString()}", d) }
        val part = read("$base/part.yaml", "part.schema.json", d, required = true)
        val sources = read("$base/sources.yaml", "sources.schema.json", d, required = true)
        val pins = read("$base/pins.yaml", "pins.schema.json", d, required = false)
        val gotchas = read("$base/gotchas.yaml", "gotchas.schema.json", d, required = false)
        val broken = listOf(part, sources, pins, gotchas).any { it == Read.Broken }
        return if (broken || part !is Read.Ok || sources !is Read.Ok) {
            null
        } else {
            val files =
                PartFiles(
                    sources = mapSources(sources.tree, "$base/sources.yaml"),
                    pins = (pins as? Read.Ok)?.let { mapPins(it.tree, "$base/pins.yaml") },
                    gotchas = (gotchas as? Read.Ok)?.let { mapGotchas(it.tree, "$base/gotchas.yaml") },
                    article = File(contentDir, "$base/article.md").takeIf { it.isFile }?.readText(),
                )
            mapPart(part.tree, folder, "$base/part.yaml", files).also {
                if (it.id != folder) d.error(it.at("id"), "id \"${it.id}\" doesn't match its folder \"$folder\"")
            }
        }
    }

    private fun read(path: String, schema: String, d: Diagnostics, required: Boolean): Read {
        val file = File(contentDir, path)
        return when {
            file.isFile -> parse(path, file.readText(), schema, d)
            required -> Read.Broken.also { d.error(Pos(path, 1, 1), "this file is missing") }
            else -> Read.Absent
        }
    }

    private fun parse(path: String, text: String, schema: String, d: Diagnostics): Read {
        val tree = yaml.read(path, text, d)
        return if (tree != null && schemas.check(schema, tree, path, d)) Read.Ok(tree) else Read.Broken
    }

    private fun unexpected(path: String, rule: String, d: Diagnostics) {
        d.error(Pos(path, 1, 1), "unexpected file: $rule")
    }

    private sealed interface Read {
        data object Absent : Read

        data object Broken : Read

        data class Ok(val tree: JsonNode) : Read
    }

    private companion object {
        val VERSION = Regex("^[0-9]+\\.[0-9]+\\.[0-9]+$")
        val PART_FILES = listOf("part.yaml", "sources.yaml", "pins.yaml", "gotchas.yaml", "article.md")
    }
}
