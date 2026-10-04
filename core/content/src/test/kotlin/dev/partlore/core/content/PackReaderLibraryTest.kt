package dev.partlore.core.content

import dev.partlore.core.model.CategoryTile
import dev.partlore.core.model.VerificationLevel
import dev.partlore.tools.content.ship.Mode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PackReaderLibraryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun threeParts(mode: Mode = Mode.RELEASE) = buildPack(tmp, mode) {
        write("categories.yaml", TREE)
        write("tags.yaml", TAGS)
        board("testmaker/zeta-board", "Zeta Board", "esp32-boards", tags = "[wifi]")
        board("testmaker/alpha-board", "Alpha Board", "arduino-boards", tags = "[usb-b, test-tag]")
        board("testmaker/radio-module", "Radio Module", "modules", kind = "module")
    }

    @Test
    fun topCategoriesCountTheirSubCategoriesInSortOrder() {
        openPack(threeParts()).use { reader ->
            val home = reader.library()
            assertEquals(
                listOf(CategoryTile("boards", "Boards", 2), CategoryTile("modules", "Modules", 1)),
                home.categories,
            )
            assertEquals(3, home.partCount)
        }
    }

    @Test
    fun starterBoardsAreTheBoardsAToZ() {
        openPack(threeParts()).use { reader ->
            val boards = reader.library().starterBoards
            assertEquals(listOf("Alpha Board", "Zeta Board"), boards.map { it.name })
            assertEquals(VerificationLevel.Checked, boards.first().level)
            assertEquals(listOf("Test tag", "USB-B"), boards.first().tags.map { it.label })
        }
    }

    @Test
    fun starterBoardsStopAtTwelve() {
        val pack =
            buildPack(tmp) {
                repeat(13) { board("testmaker/board-%02d".format(it), "Board %02d".format(it), "test-boards") }
            }
        openPack(pack).use { assertEquals(12, it.library().starterBoards.size) }
    }

    @Test
    fun previewPacksSayTheyArePreviews() {
        openPack(threeParts(Mode.PREVIEW)).use { assertTrue(it.library().isPreview) }
        openPack(threeParts()).use { assertFalse(it.library().isPreview) }
    }

    @Test
    fun anEmptyPackHasNoParts() {
        openPack(buildPack(tmp) {}).use { reader ->
            val home = reader.library()
            assertEquals(0, home.partCount)
            assertTrue(home.starterBoards.isEmpty())
        }
    }
}
