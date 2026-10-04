package dev.partlore.tools.content.ship

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import dev.partlore.tools.content.testing.TODAY
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ShippingTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    private fun level(file: String, level: String) =
        fixture.edit("$BOARD_DIR/$file") { it.replaceFirst("level: checked", "level: $level") }

    private fun part(): Part = Validator(fixture.root, TODAY).validate(Mode.PREVIEW).content.parts.single()

    @Test
    fun aCheckedPartShipsWithEverything() {
        val part = part()
        assertEquals(listOf(true, true, true, true), ship(part, Mode.RELEASE))
    }

    @Test
    fun aDraftPartShipsOnlyInPreview() {
        level("part.yaml", "draft")
        val part = part()
        assertEquals(listOf(false, false, false, false), ship(part, Mode.RELEASE))
        assertEquals(listOf(true, true, true, true), ship(part, Mode.PREVIEW))
    }

    @Test
    fun importedPinsWaitForAPersonButThePartShips() {
        level("pins.yaml", "draft")
        assertEquals(listOf(true, false, true, true), ship(part(), Mode.RELEASE))
    }

    private fun ship(part: Part, mode: Mode) = listOf(
        Shipping.partShips(part, mode),
        Shipping.pinsShip(part, mode),
        Shipping.gotchasShip(part, mode),
        Shipping.articleShips(part, mode),
    )
}
