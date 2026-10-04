package dev.partlore.tools.content.load

import dev.partlore.tools.content.diag.Diagnostic
import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Level
import dev.partlore.tools.content.model.Scalar
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ContentLoaderTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content"))
    }

    private fun load(): Pair<Content, List<Diagnostic>> {
        val d = Diagnostics()
        return ContentLoader(fixture.root).load(d) to d.all
    }

    @Test
    fun aValidBoardBecomesTheModel() {
        fixture.addBoard()
        val (content, problems) = load()
        assertEquals(emptyList<Diagnostic>(), problems)
        val part = content.parts.single()
        assertEquals(BOARD, part.id)
        assertEquals("Test Board", part.name)
        assertEquals(Level.CHECKED, part.status.level)
        assertEquals(Scalar.Num(3.3), part.specs.single { it.key == "io.logic_level" }.value)
        assertEquals(0x3c, part.i2c.single().address)
        val gpio1 = part.pins!!.pins.first()
        assertEquals(listOf("GPIO1", "D1", "SDA", "1"), gpio1.names)
        assertEquals("high", part.pins.pins[1].strapping?.mustBe)
        assertEquals(listOf("gpio0"), part.gotchas!!.gotchas.single().pins)
        assertEquals("A board that exists only in tests.\n", part.article)
    }

    @Test
    fun emptyContentLoads() {
        val (content, problems) = load()
        assertEquals(emptyList<Diagnostic>(), problems)
        assertTrue(content.parts.isEmpty())
        assertEquals("0.0.1", content.version)
    }

    @Test
    fun partsAreSortedById() {
        fixture.addBoard("testmaker/b-board")
        fixture.addBoard("testmaker/a-board")
        assertEquals(listOf("testmaker/a-board", "testmaker/b-board"), load().first.parts.map { it.id })
    }

    @Test
    fun loadingManyPartsTogetherLosesNothing() {
        val ids = (0 until MANY).map { "testmaker/board-%02d".format(it) }
        ids.forEach { fixture.addBoard(it) }
        val broken = ids.filterIndexed { i, _ -> i % 2 == 0 }
        broken.forEach { id -> fixture.edit("parts/$id/pins.yaml") { it.replaceFirst("voltage: 3.3", "votlage: 3.3") } }
        val (content, problems) = load()
        assertEquals(broken.map { "parts/$it/pins.yaml" }, problems.map { it.file })
        assertEquals(broken.toSet(), content.unreadable)
        assertEquals(ids - broken.toSet(), content.parts.map { it.id })
    }

    @Test
    fun aStrappingPinMayLeaveOutTheLevelItNeeds() {
        fixture.addBoard()
        fixture.edit("$BOARD_DIR/pins.yaml") { it.replace("must_be: high, ", "") }
        val (content, problems) = load()
        assertEquals(emptyList<Diagnostic>(), problems)
        val strapping = content.parts.single().pins!!.pins[1].strapping
        assertEquals("boot-mode", strapping?.role)
        assertEquals(null, strapping?.mustBe)
    }

    @Test
    fun theIdMustMatchItsFolder() {
        fixture.addBoard()
        fixture.edit("$BOARD_DIR/part.yaml") { it.replace("id: $BOARD", "id: testmaker/other-board") }
        val problem = load().second.single()
        assertEquals("$BOARD_DIR/part.yaml", problem.file)
        assertEquals(1, problem.line)
        assertTrue(problem.message, problem.message.contains("doesn't match its folder"))
    }

    @Test
    fun aSchemaErrorLeavesThePartOutAndNamesTheLine() {
        fixture.addBoard()
        fixture.edit("$BOARD_DIR/pins.yaml") { it.replaceFirst("voltage: 3.3", "votlage: 3.3") }
        val (content, problems) = load()
        assertTrue(content.parts.isEmpty())
        assertEquals(setOf(BOARD), content.unreadable)
        val problem = problems.single()
        assertEquals(fixture.lineOf("$BOARD_DIR/pins.yaml", "votlage"), problem.line)
    }

    @Test
    fun aMissingSourcesFileIsAnError() {
        fixture.addBoard()
        fixture.delete("$BOARD_DIR/sources.yaml")
        val problem = load().second.single()
        assertEquals("$BOARD_DIR/sources.yaml", problem.file)
    }

    @Test
    fun anUnexpectedFileInAPartFolderIsAnError() {
        fixture.addBoard()
        fixture.write("$BOARD_DIR/notes.txt", "x")
        assertEquals("$BOARD_DIR/notes.txt", load().second.single().file)
    }

    @Test
    fun aBadVersionIsAnError() {
        fixture.write("version.txt", "one\n")
        assertEquals("version.txt", load().second.single().file)
    }

    private companion object {
        const val MANY = 40
    }
}
