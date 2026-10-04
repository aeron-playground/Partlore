package dev.partlore.tools.packer

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PackWriterTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    private fun pack(mode: Mode = Mode.RELEASE): File {
        val result = Validator(fixture.root, TODAY).validate(mode)
        check(!result.hasErrors) { result.diagnostics.joinToString("\n") { it.plain("") } }
        return File(tmp.root, "pack-${mode.name}.db").also { PackWriter().write(result.content, mode, "test-hash", it) }
    }

    @Test
    fun aCheckedBoardIsWrittenCompletely() {
        val pack = pack()
        assertEquals(listOf(listOf("Test Board", "testmaker")), query(pack, "SELECT name, manufacturer FROM part"))
        assertEquals("3", single(pack, "SELECT count(*) FROM pin"))
        assertEquals("60", single(pack, "SELECT address FROM i2c_address"))
        val files = query(pack, "SELECT file FROM status ORDER BY file").map { it.single() }
        assertEquals(listOf("gotchas", "part", "pins"), files)
        assertEquals("1", single(pack, "SELECT value FROM meta WHERE key = 'schema_version'"))
        assertEquals("test-hash", single(pack, "SELECT value FROM meta WHERE key = 'content_sha256'"))
    }

    @Test
    fun theGlanceStripIsWorkedOutFromThePins() {
        assertEquals(
            listOf(listOf("2", "1", "none", "3.3", "802.11 b/g/n")),
            query(pack(), "SELECT gpio_count, adc_channels, five_v_tolerant, logic_level_v, wifi FROM glance"),
        )
    }

    @Test
    fun searchFindsPiecesOfNamesAndPins() {
        val pack = pack()
        assertEquals(BOARD, single(pack, "SELECT DISTINCT part_id FROM search WHERE search MATCH ?", "\"est Bo\""))
        assertEquals("gpio1", single(pack, "SELECT ref FROM search WHERE search MATCH ?", "\"GPIO1\""))
        assertEquals("gpio1", single(pack, "SELECT pin_id FROM pin_alias WHERE label_lower = ?", "d1"))
    }

    @Test
    fun releaseLeavesDraftPartsOutAndPreviewKeepsThem() {
        fixture.addBoard("testmaker/draft-board")
        fixture.edit("parts/testmaker/draft-board/part.yaml") { it.replace("level: checked", "level: draft") }
        assertEquals("1", single(pack(Mode.RELEASE), "SELECT count(*) FROM part"))
        assertEquals("2", single(pack(Mode.PREVIEW), "SELECT count(*) FROM part"))
    }

    @Test
    fun uncheckedPinsStayOutButThePartShips() {
        fixture.edit("$BOARD_DIR/pins.yaml") { it.replace("level: checked", "level: draft") }
        fixture.edit("$BOARD_DIR/gotchas.yaml") { it.replace("level: checked", "level: draft") }
        val pack = pack()
        assertEquals("1", single(pack, "SELECT count(*) FROM part"))
        assertEquals("0", single(pack, "SELECT count(*) FROM pin"))
        assertEquals(null, single(pack, "SELECT gpio_count FROM glance"))
        assertEquals(listOf("part"), query(pack, "SELECT file FROM status").map { it.single() })
    }

    @Test
    fun unicodeSurvivesThePackAndIsSearchable() {
        fixture.edit("$BOARD_DIR/part.yaml") { it.replace("name: Test Board", "name: Thermo µΩ Board 25 °C") }
        val pack = pack()
        assertEquals("Thermo µΩ Board 25 °C", single(pack, "SELECT name FROM part"))
        assertEquals(BOARD, single(pack, "SELECT DISTINCT part_id FROM search WHERE search MATCH ?", "\"µΩ B\""))
    }
}
