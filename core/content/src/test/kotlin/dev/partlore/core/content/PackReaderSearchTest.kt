package dev.partlore.core.content

import dev.partlore.core.model.SearchResults
import dev.partlore.core.model.Severity
import dev.partlore.core.model.VerificationLevel
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// Fixture pins on every board: gpio1 (GPIO1, D1, SDA, Arduino 1), gpio0 (GPIO0, D0), gnd (GND).
class PackReaderSearchTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun ContentFixture.boards() {
        write("categories.yaml", TREE)
        write("tags.yaml", TAGS)
        board("makera/alpha-board", "Alpha Board", "esp32-boards")
        board("makerb/zeta-board", "Zeta Board", "arduino-boards")
    }

    private fun search(text: String, mode: Mode = Mode.RELEASE, setup: ContentFixture.() -> Unit = { boards() }) =
        openPack(buildPack(tmp, mode, setup)).use { it.search(text) }

    private fun SearchResults.pinKeys() = pins.map { "${it.partId}:${it.pinId}" }

    @Test
    fun nothingTypedFindsNothing() {
        assertEquals(SearchResults(), search("   "))
    }

    @Test
    fun aPartIsFoundByItsNameMakerOrAlias() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            assertEquals(listOf("makera/alpha-board"), reader.search("alpha").parts.map { it.id })
            assertEquals(listOf("makerb/zeta-board"), reader.search("MAKERB").parts.map { it.id })
            assertEquals(2, reader.search("tb-1").parts.size)
        }
    }

    @Test
    fun aPartHitSaysWhatItIs() {
        val hit = search("alpha").parts.single()
        assertEquals("Alpha Board", hit.name)
        assertEquals("makera", hit.manufacturer)
        assertEquals("board", hit.kind)
        assertEquals(VerificationLevel.Checked, hit.level)
        assertEquals(null, hit.alias)
    }

    @Test
    fun anAliasIsShownOnlyWhenItIsWhatMatched() {
        assertEquals(listOf("TB-1", "TB-1"), search("tb-1").parts.map { it.alias })
    }

    @Test
    fun aPartMustMatchEveryWord() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            assertEquals(listOf("Alpha Board"), reader.search("board makera").parts.map { it.name })
            assertEquals(emptyList<String>(), reader.search("alpha makerb").parts.map { it.name })
        }
    }

    @Test
    fun aShortWordCanNameAPart() {
        val hits = search("d1 mini") {
            boards()
            board("makerc/d1-mini", "D1 Mini", "esp32-boards")
        }
        assertEquals(listOf("D1 Mini"), hits.parts.map { it.name })
    }

    @Test
    fun theBestNameMatchComesFirst() {
        val hits = search("uno") {
            write("categories.yaml", TREE)
            write("tags.yaml", TAGS)
            board("m/a-shield", "Almost Uno Shield", "esp32-boards")
            board("m/unit", "Zeta", "esp32-boards")
            board("m/uno-wifi", "Uno WiFi", "esp32-boards")
            board("m/uno", "UNO", "esp32-boards")
            edit("parts/m/unit/part.yaml") { it.replace("aliases: [TB-1]", "aliases: [Uno clone]") }
        }
        assertEquals(listOf("UNO", "Uno WiFi", "Almost Uno Shield", "Zeta"), hits.parts.map { it.name })
    }

    @Test
    fun aTwoLetterPinNameIsFound() {
        val hits = search("D1")
        assertEquals(listOf("makera/alpha-board:gpio1", "makerb/zeta-board:gpio1"), hits.pinKeys())
        val pins = hits.pins
        assertEquals("D1", pins.first().label)
        assertTrue(pins.first().exact)
        assertEquals("Alpha Board", pins.first().partName)
        assertEquals(listOf("GPIO1", "SDA", "1"), pins.first().otherNames)
    }

    @Test
    fun aPartWordNarrowsThePins() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            assertEquals(listOf("makera/alpha-board:gpio1"), reader.search("alpha d1").pinKeys())
            assertEquals(listOf("makerb/zeta-board:gpio1"), reader.search("zeta sda").pinKeys())
            assertEquals(listOf("makera/alpha-board:gpio0"), reader.search("alpha GPIO 0").pinKeys())
            assertEquals(listOf("makerb/zeta-board:gpio1"), reader.search("d1 makerb").pinKeys())
        }
    }

    @Test
    fun aPinHitDoesNotAlsoListThePart() {
        assertEquals(emptyList<String>(), search("alpha d1").parts.map { it.id })
    }

    @Test
    fun aPlainNumberIsAPinNumber() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            // gpio1 is the Arduino pin "1", and GPIO1 and D1 too; it still shows once.
            assertEquals(listOf("makera/alpha-board:gpio1"), reader.search("alpha 1").pinKeys())
            assertEquals(listOf("makera/alpha-board:gpio0"), reader.search("alpha pin 0").pinKeys())
        }
    }

    @Test
    fun partOfAPinNameFindsItOnlyFromThreeLetters() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            val pins = reader.search("alpha gpio").pins
            assertEquals(listOf("GPIO0", "GPIO1"), pins.map { it.label })
            assertFalse(pins.any { it.exact })
            assertEquals(4, reader.search("gpi").pins.size)
            assertEquals(emptyList<String>(), reader.search("gp").pins.map { it.label })
        }
    }

    @Test
    fun exactPinsComeFirstThenNumbersInOrder() {
        val pins = search("gpio1") {
            write("categories.yaml", TREE)
            write("tags.yaml", TAGS)
            addBoard("makera/alpha-board", extraPins = 2)
            edit("parts/makera/alpha-board/part.yaml") { it.replace("category: test-boards", "category: modules") }
            edit("parts/makera/alpha-board/pins.yaml") {
                it.replace("board_label: \"IO4\"", "chip_name: GPIO12\n    board_label: \"IO4\"")
                    .replace("board_label: \"IO5\"", "chip_name: GPIO10\n    board_label: \"IO5\"")
            }
        }.pins
        assertEquals(listOf("GPIO1", "GPIO10", "GPIO12"), pins.map { it.label })
        assertEquals(listOf(true, false, false), pins.map { it.exact })
    }

    @Test
    fun oddInputNeverBreaksASearch() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            listOf(
                "\"", "\"\"", "\"abc", "a\"b\"c", "*", "abc*", "(", ")", "AND", "NOT x", "x OR y", "col:x", "^abc",
                "NEAR(a b)", "%", "_", "\\", "'", "';--", "😀", "😀😀😀", "é",
            ).forEach { reader.search(it) }
        }
    }

    @Test
    fun wildcardsAreOrdinaryLetters() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            assertEquals(SearchResults(), reader.search("%"))
            assertEquals(SearchResults(), reader.search("_"))
            assertEquals(SearchResults(), reader.search("alp%a"))
        }
    }

    @Test
    fun aQuoteInAPinWordIsNotASearchCommand() {
        assertEquals(emptyList<String>(), search("gpio\"").pins.map { it.label })
    }

    @Test
    fun pinsThatDoNotShipAreNeverFound() {
        val unchecked: ContentFixture.() -> Unit = {
            boards()
            edit("parts/makera/alpha-board/pins.yaml") { it.replace("level: checked", "level: draft") }
            delete("parts/makera/alpha-board/gotchas.yaml")
        }
        assertEquals(listOf("makerb/zeta-board:gpio1"), search("d1", Mode.RELEASE, unchecked).pinKeys())
        assertEquals(2, search("gpio1", Mode.PREVIEW, unchecked).pins.size)
    }

    @Test
    fun aGotchaIsFoundByItsTitleOrBody() {
        openPack(buildPack(tmp) { boards() }).use { reader ->
            assertEquals(2, reader.search("high at reset").gotchas.size)
            val hit = reader.search("alpha bootloader").gotchas.single()
            assertEquals("makera/alpha-board", hit.partId)
            assertEquals("Alpha Board", hit.partName)
            assertEquals("boot-pin", hit.id)
            assertEquals("GPIO0 must be high at reset", hit.title)
            assertEquals(Severity.Caution, hit.severity)
        }
    }

    @Test
    fun aGotchaNeedsAWordOfItsOwn() {
        assertEquals(emptyList<String>(), search("alpha").gotchas.map { it.id })
    }

    @Test
    fun dangerousGotchasComeFirst() {
        val hits = search("bootloader") {
            boards()
            edit("parts/makerb/zeta-board/gotchas.yaml") { it.replace("severity: caution", "severity: danger") }
        }
        assertEquals(listOf(Severity.Danger, Severity.Caution), hits.gotchas.map { it.severity })
    }

    @Test
    fun aLongListIsCutAndSaysSo() {
        val many: ContentFixture.() -> Unit = {
            repeat(35) { n -> addBoard("many/board-%02d".format(n)) }
        }
        openPack(buildPack(tmp, setup = many)).use { reader ->
            val parts = reader.search("test board")
            assertEquals(30, parts.parts.size)
            assertTrue(parts.more)
            val pins = reader.search("d1")
            assertEquals(30, pins.pins.size)
            assertTrue(pins.more)
            val gotchas = reader.search("bootloader")
            assertEquals(30, gotchas.gotchas.size)
            assertTrue(gotchas.more)
        }
    }

    @Test
    fun aShortListSaysThereIsNoMore() {
        assertFalse(search("board d1").more)
    }
}
