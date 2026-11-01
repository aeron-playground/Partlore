package dev.partlore.core.content

import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.packer.Packer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// What a person gets from the real content in a release pack, so the app's suggestions never lead nowhere.
class RealSearchTest {
    private fun search(text: String) = openPack(pack).use { it.search(text) }

    private fun assertPin(text: String, part: String, pin: String) {
        val hit = search(text).pins.firstOrNull { it.partId == part && it.pinId == pin }
        assertTrue("\"$text\" should find $pin on $part, got ${search(text).pins}", hit != null)
    }

    @Test
    fun aPartAndAPinTogetherFindThatPin() {
        assertPin("esp32 gpio 34", "espressif/esp32-devkitc-v4-wroom-32e", "gpio34")
        assertPin("devkitc gpio34", "espressif/esp32-devkitc-v4-wroom-32e", "gpio34")
        assertPin("esp32 34", "espressif/esp32-devkitc-v4-wroom-32e", "gpio34")
    }

    @Test
    fun aBoardLabelOfTwoLettersIsFound() {
        assertPin("uno d4", "arduino/uno-r3", "d4")
        assertPin("d4", "arduino/uno-r3", "d4")
    }

    @Test
    fun theAppsSuggestionsAllFindSomething() {
        SEARCH_SUGGESTIONS_IN_THE_APP.forEach { assertTrue("\"$it\" finds nothing", !search(it).isEmpty) }
    }

    @Test
    fun boardsNobodyHasCheckedShowUpWithoutTheirPins() {
        val hits = search("esp32-s3-devkitc gpio4")
        assertEquals(emptyList<String>(), hits.pins.map { it.partId })
        assertEquals(
            listOf("espressif/esp32-s3-devkitc-1"),
            search("esp32-s3-devkitc").parts.map { it.id },
        )
    }

    companion object {
        // The same words as search_suggestions in feature/search.
        private val SEARCH_SUGGESTIONS_IN_THE_APP = listOf("esp32 gpio 34", "uno d4", "boot mode")

        @get:ClassRule
        @JvmStatic
        val tmp = TemporaryFolder()

        private val pack: File by lazy {
            val content = File(System.getProperty("partlore.contentDir"))
            val result = Packer(content, tmp.newFolder()).pack(Mode.RELEASE)
            check(result is Packer.Result.Packed) { result.diagnostics.take(5).joinToString("\n") { it.plain("") } }
            result.file
        }
    }
}
