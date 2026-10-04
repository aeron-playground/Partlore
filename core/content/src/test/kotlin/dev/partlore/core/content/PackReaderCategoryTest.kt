package dev.partlore.core.content

import dev.partlore.core.model.CategoryTile
import dev.partlore.core.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PackReaderCategoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun pack() = buildPack(tmp) {
        write("categories.yaml", TREE)
        write("tags.yaml", TAGS)
        board("testmaker/zeta-board", "Zeta Board", "esp32-boards", tags = "[wifi]")
        board("testmaker/alpha-board", "Alpha Board", "arduino-boards", tags = "[usb-b, test-tag]")
        board("testmaker/radio-module", "Radio Module", "modules", kind = "module")
    }

    @Test
    fun aTopCategoryListsThePartsOfItsChildren() {
        openPack(pack()).use { reader ->
            val page = checkNotNull(reader.category("boards"))
            assertEquals("Boards", page.name)
            assertEquals(listOf("Alpha Board", "Zeta Board"), page.parts.map { it.name })
            assertEquals(
                listOf(
                    CategoryTile("esp32-boards", "ESP32 boards", 1),
                    CategoryTile("arduino-boards", "Arduino boards", 1),
                ),
                page.children,
            )
        }
    }

    @Test
    fun childChipsKnowTheirParts() {
        openPack(pack()).use { reader ->
            val page = checkNotNull(reader.category("boards"))
            assertEquals(setOf("testmaker/zeta-board"), page.partsByChild["esp32-boards"])
            assertEquals(setOf("testmaker/alpha-board"), page.partsByChild["arduino-boards"])
        }
    }

    @Test
    fun tagChipsAreTheTagsOfTheListedPartsAToZ() {
        openPack(pack()).use { reader ->
            assertEquals(
                listOf(Tag("test-tag", "Test tag"), Tag("usb-b", "USB-B"), Tag("wifi", "Wi-Fi")),
                checkNotNull(reader.category("boards")).tags,
            )
        }
    }

    @Test
    fun aLeafCategoryHasNoChildren() {
        openPack(pack()).use { reader ->
            val page = checkNotNull(reader.category("modules"))
            assertEquals(emptyList<CategoryTile>(), page.children)
            assertEquals(listOf("Radio Module"), page.parts.map { it.name })
        }
    }

    @Test
    fun anUnknownCategoryIsNull() {
        openPack(pack()).use { assertNull(it.category("nothing")) }
    }
}
