package dev.partlore.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryTest {
    private fun words(text: String) = SearchQuery.parse(text).words

    @Test
    fun wordsAreSplitAndLowercased() {
        assertEquals(listOf("esp32", "devkitc"), words("  ESP32 \t DevKitC "))
    }

    @Test
    fun nothingTypedMeansNoWords() {
        assertEquals(emptyList<String>(), words(""))
        assertEquals(emptyList<String>(), words(" \n\t "))
    }

    @Test
    fun aNumberJoinsThePinPrefixBeforeIt() {
        assertEquals(listOf("esp32", "gpio34"), words("esp32 GPIO 34"))
        assertEquals(listOf("d4"), words("d 4"))
        assertEquals(listOf("io2"), words("IO 2"))
        assertEquals(listOf("gp15"), words("gp 15"))
        assertEquals(listOf("a0"), words("a 0"))
    }

    @Test
    fun aNumberAfterAnyOtherWordStaysApart() {
        assertEquals(listOf("uno", "3"), words("uno 3"))
        assertEquals(listOf("esp32", "34"), words("esp32 34"))
        assertEquals(listOf("34", "gpio"), words("34 gpio"))
        assertEquals(listOf("gpio", "esp32"), words("gpio esp32"))
    }

    @Test
    fun theWordPinBeforeANumberIsDropped() {
        assertEquals(listOf("esp32", "34"), words("esp32 pin 34"))
        assertEquals(listOf("pin"), words("pin"))
        assertEquals(listOf("pin", "header"), words("pin header"))
    }

    @Test
    fun controlCharactersAndOtherSpacesSplitWords() {
        assertEquals(listOf("a", "b", "c", "d"), words("a\u0000b\nc d"))
    }

    @Test
    fun aLongPasteIsCut() {
        assertEquals(List(6) { "w$it" }, words((0 until 500).joinToString(" ") { "w$it" }))
        assertEquals(listOf("x".repeat(64)), words("x".repeat(10_000)))
    }
}
