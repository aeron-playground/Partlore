package dev.partlore.tools.content.rules

import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import dev.partlore.tools.content.testing.assertNoProblems
import dev.partlore.tools.content.testing.assertProblem
import dev.partlore.tools.content.testing.problems
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LinkRulesTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture
    private val part = "$BOARD_DIR/part.yaml"
    private val pins = "$BOARD_DIR/pins.yaml"

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    @Test
    fun aValidBoardHasNoProblems() = assertNoProblems(fixture.problems())

    @Test
    fun aCitationToAMissingSourceIsAnError() {
        fixture.edit(part) { it.replace("source: s2, section: Radio", "source: s9, section: Radio") }
        val line = fixture.lineOf(part, "source: s9")
        assertProblem(fixture.problems(), part, line, "source \"s9\" is not in sources.yaml")
    }

    @Test
    fun aPdfSourceNeedsAPage() {
        fixture.edit(pins) { it.replaceFirst("- { source: s1, page: 5 }", "- { source: s1, section: Pins }") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "section: Pins"), "is a datasheet: cite a page")
    }

    @Test
    fun aWebSourceNeedsASection() {
        fixture.edit(part) { it.replace("source: s2, section: Radio", "source: s2, page: 1") }
        val line = fixture.lineOf(part, "source: s2, page: 1")
        assertProblem(fixture.problems(), part, line, "is a web page: cite a section")
    }

    @Test
    fun aSpecKeyMustExist() {
        fixture.edit(part) { it.replace("key: supply.voltage, unit: V, min", "key: supply.voltagee, unit: V, min") }
        assertProblem(fixture.problems(), part, fixture.lineOf(part, "voltagee"), "spec key \"supply.voltagee\"")
    }

    @Test
    fun aSpecUnitMustMatchItsKey() {
        fixture.edit(part) { it.replace("key: supply.voltage, unit: V, min", "key: supply.voltage, unit: mV, min") }
        assertProblem(fixture.problems(), part, fixture.lineOf(part, "unit: mV"), "measured in V")
    }

    @Test
    fun aRangeKeyNeedsMinTypOrMax() {
        fixture.edit(part) { it.replace("min: 3.0, typ: 3.3, max: 3.6, source: s1", "value: 3.3, source: s1") }
        assertProblem(fixture.problems(), part, fixture.lineOf(part, "supply.voltage, unit: V, value"), "is a range")
    }

    @Test
    fun theCategoryAndTagsMustExist() {
        fixture.edit(part) {
            it.replace("category: test-boards", "category: test-board").replace("[test-tag]", "[no-tag]")
        }
        val problems = fixture.problems()
        assertProblem(problems, part, fixture.lineOf(part, "category:"), "category \"test-board\"")
        assertProblem(problems, part, fixture.lineOf(part, "tags:"), "tag \"no-tag\"")
    }

    @Test
    fun usesMustPointToAPart() {
        fixture.edit(part) { it.replace("summary:", "uses: testmaker/nothing\nsummary:") }
        val line = fixture.lineOf(part, "uses:")
        assertProblem(fixture.problems(), part, line, "part \"testmaker/nothing\" doesn't exist")
    }

    @Test
    fun aBrokenPartCausesNoErrorsElsewhere() {
        fixture.addBoard("testmaker/other-board")
        fixture.edit("parts/testmaker/other-board/pins.yaml") { it.replaceFirst("voltage: 3.3", "votlage: 3.3") }
        fixture.edit(part) { it.replace("summary:", "related: [testmaker/other-board]\nsummary:") }
        val problems = fixture.problems()
        assertEquals(listOf("parts/testmaker/other-board/pins.yaml"), problems.map { it.file })
    }

    @Test
    fun theMakerMustMatchTheManufacturer() {
        fixture.edit(part) { it.replace("manufacturer: testmaker", "manufacturer: Other Maker") }
        assertProblem(fixture.problems(), part, fixture.lineOf(part, "manufacturer:"), "gives other-maker/")
    }

    @Test
    fun twoPinsCantShareAPlace() {
        fixture.edit(pins) { it.replace("index: 2", "index: 1") }
        val problems = fixture.problems()
        assertProblem(problems, pins, fixture.lineOf(pins, "- id: gpio0"), "same place as gpio1")
        assertProblem(problems, pins, fixture.lineOf(pins, "- id: j1"), "no pin at row 1, index 2")
    }

    @Test
    fun aPinMustFitItsHeader() {
        fixture.edit(pins) { it.replace("index: 3", "index: 4") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: gnd"), "outside header j1")
    }

    @Test
    fun everyPositionNeedsAPin() {
        fixture.edit(pins) { it.replace("pins_per_row: 3", "pins_per_row: 4") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: j1"), "no pin at row 1, index 4")
    }

    @Test
    fun aGotchaPinMustExist() {
        val gotchas = "$BOARD_DIR/gotchas.yaml"
        fixture.edit(gotchas) { it.replace("pins: [gpio0]", "pins: [gpio9]") }
        assertProblem(fixture.problems(), gotchas, fixture.lineOf(gotchas, "- id: boot-pin"), "pin \"gpio9\"")
    }

    @Test
    fun aSourceIdIsListedOnce() {
        val sources = "$BOARD_DIR/sources.yaml"
        fixture.edit(sources) { it.replace("- id: s2", "- id: s1") }
        val line = fixture.lineOf(sources, "type: web-doc") - 1
        assertProblem(fixture.problems(), sources, line, "source \"s1\" is listed twice")
    }

    @Test
    fun anI2cAddressIsListedOnce() {
        fixture.edit(part) {
            it.replace("i2c:\n", "i2c:\n  - { address: \"0x3c\", select: \"again\", source: s1, page: 4 }\n")
        }
        val line = fixture.lineOf(part, "select: \"ADDR")
        assertProblem(fixture.problems(), part, line, "I²C address \"0x3c\" is listed twice")
    }

    @Test
    fun sharedFilesAreCheckedToo() {
        fixture.write("categories.yaml", "categories:\n  - id: test-boards\n    name: A\n    parent: nowhere\n")
        fixture.edit("schema/spec-keys.yaml") { it.replace("type: text\n", "type: text\n    unit: V\n") }
        val problems = fixture.problems()
        assertProblem(problems, "categories.yaml", 2, "parent \"nowhere\"")
        val keys = "schema/spec-keys.yaml"
        assertProblem(problems, keys, fixture.lineOf(keys, "key: flash.size"), "can't have a unit")
    }
}
