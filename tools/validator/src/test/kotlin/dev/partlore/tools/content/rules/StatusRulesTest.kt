package dev.partlore.tools.content.rules

import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import dev.partlore.tools.content.testing.assertNoProblems
import dev.partlore.tools.content.testing.assertProblem
import dev.partlore.tools.content.testing.problems
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class StatusRulesTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture
    private val part = "$BOARD_DIR/part.yaml"
    private val pins = "$BOARD_DIR/pins.yaml"
    private val gotchas = "$BOARD_DIR/gotchas.yaml"

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    private fun statusLine(file: String) = fixture.lineOf(file, "level:")

    @Test
    fun checkedNeedsWhoWhenAndAgainst() {
        fixture.edit(part) { it.replace("  checked_by: [tester]\n", "") }
        assertProblem(fixture.problems(), part, statusLine(part), "checked needs checked_by")
    }

    @Test
    fun verifiedNeedsTwoPeople() {
        fixture.edit(part) { it.replace("level: checked", "level: verified") }
        assertProblem(fixture.problems(), part, statusLine(part), "two different people")
    }

    @Test
    fun aCheckCantBeInTheFuture() {
        fixture.edit(part) { it.replace("checked_on: 2026-01-01", "checked_on: 2027-01-01") }
        assertProblem(fixture.problems(), part, statusLine(part), "is in the future")
    }

    @Test
    fun aCheckDateMustBeReal() {
        fixture.edit(part) { it.replace("checked_on: 2026-01-01", "checked_on: 2026-13-01") }
        assertProblem(fixture.problems(), part, statusLine(part), "not a real date")
    }

    @Test
    fun importedNeedsImportedFrom() {
        fixture.edit(part) { it.replace("level: checked", "level: imported") }
        assertProblem(fixture.problems(), part, statusLine(part), "imported needs imported_from")
    }

    @Test
    fun gotchasAreNeverImported() {
        fixture.edit(gotchas) {
            it.replace(
                "level: checked",
                "level: imported\n  imported_from: { importer: test, source: s1, ref: x, on: 2026-01-01 }",
            )
        }
        assertProblem(fixture.problems(), gotchas, statusLine(gotchas), "can't be imported")
    }

    @Test
    fun needsReviewNeedsANote() {
        fixture.edit(pins) { it.replace("level: checked", "level: needs-review") }
        assertProblem(fixture.problems(), pins, statusLine(pins), "needs a note")
    }

    @Test
    fun aReleasedPartCantUseADraft() {
        fixture.addBoard("testmaker/draft-board")
        fixture.edit("parts/testmaker/draft-board/part.yaml") { it.replace("level: checked", "level: draft") }
        fixture.edit(part) { it.replace("summary:", "uses: testmaker/draft-board\nsummary:") }
        assertProblem(fixture.problems(), part, fixture.lineOf(part, "uses:"), "isn't in release packs yet")
        assertNoProblems(fixture.problems(Mode.PREVIEW))
    }

    @Test
    fun releasedGotchasCantNameUncheckedPins() {
        fixture.edit(pins) { it.replace("level: checked", "level: draft") }
        assertProblem(fixture.problems(), gotchas, statusLine(gotchas), "pins.yaml isn't checked yet")
    }
}
