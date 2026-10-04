package dev.partlore.core.content

import dev.partlore.core.model.ContentResult
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.packer.Packer
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate

internal val TODAY: LocalDate = LocalDate.of(2026, 10, 4)

/** A category tree: two top-level categories, one with two children. */
internal val TREE =
    """
    categories:
      - { id: boards, name: Boards, sort: 1 }
      - { id: esp32-boards, name: ESP32 boards, parent: boards, sort: 1 }
      - { id: arduino-boards, name: Arduino boards, parent: boards, sort: 2 }
      - { id: modules, name: Modules, sort: 2 }
    """.trimIndent() + "\n"

internal val TAGS =
    """
    tags:
      - { id: wifi, label: Wi-Fi }
      - { id: usb-b, label: USB-B }
      - { id: test-tag, label: Test tag }
    """.trimIndent() + "\n"

/** Adds a valid board (see ContentFixture) and changes its name, category, kind and tags. */
internal fun ContentFixture.board(
    id: String,
    name: String,
    category: String,
    kind: String = "board",
    tags: String = "[test-tag]",
) {
    addBoard(id)
    edit("parts/$id/part.yaml") {
        it.replace("name: Test Board", "name: $name")
            .replace("category: test-boards", "category: $category")
            .replace("kind: board", "kind: $kind")
            .replace("tags: [test-tag]", "tags: $tags")
    }
}

/** Builds a pack from fresh test content prepared by [setup]. */
internal fun buildPack(tmp: TemporaryFolder, mode: Mode = Mode.RELEASE, setup: ContentFixture.() -> Unit): File {
    val fixture = ContentFixture(tmp.newFolder()).apply(setup)
    val result = Packer(fixture.root, tmp.newFolder(), TODAY).pack(mode)
    check(result is Packer.Result.Packed) { result.diagnostics.joinToString("\n") { it.plain("") } }
    return result.file
}

internal fun openPack(file: File): PackReader {
    val opened = PackReader.open(file)
    check(opened is ContentResult.Ok) { "pack didn't open: $opened" }
    return opened.value
}
