package dev.partlore.tools.content.rules

import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import dev.partlore.tools.content.testing.assertNoProblems
import dev.partlore.tools.content.testing.assertProblem
import dev.partlore.tools.content.testing.problems
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HeaderRunRulesTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture
    private val pins = "$BOARD_DIR/pins.yaml"
    private val oneRun = "      - { edge: left, pins: 3, first: top, source: s1, page: 5 }"

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    private fun edges(vararg runs: String) = fixture.edit(pins) { it.replace(oneRun, runs.joinToString("\n")) }

    private fun line() = fixture.lineOf(pins, "- id: j1")

    @Test
    fun aValidLayoutHasNoProblems() = assertNoProblems(fixture.problems())

    @Test
    fun runsMustAddUpToTheHeader() {
        edges("      - { edge: left, pins: 2, first: top, source: s1, page: 5 }")
        assertProblem(fixture.problems(), pins, line(), "runs of header j1 cover 2 of 3 positions")
    }

    @Test
    fun firstMustFitTheRunDirection() {
        edges("      - { edge: left, pins: 3, first: right, source: s1, page: 5 }")
        assertProblem(fixture.problems(), pins, line(), "a vertical run starts at top or bottom")
    }

    @Test
    fun insideNeedsARunDirectionAndEdgesMustNotHaveOne() {
        edges("      - { edge: inside, pins: 3, first: top, source: s1, page: 5 }")
        assertProblem(fixture.problems(), pins, line(), "an inside run needs run: vertical or horizontal")
    }

    @Test
    fun row1IsOnlyForTwoRowHeaders() {
        edges("      - { edge: left, pins: 3, first: top, row1: left, source: s1, page: 5 }")
        assertProblem(fixture.problems(), pins, line(), "row1 is only for 2-row headers")
    }

    @Test
    fun twoRunsOnOneEdgeNeedDifferentOrders() {
        fixture.edit(pins) {
            it.replace("pins_per_row: 3", "pins_per_row: 4").replace(
                oneRun,
                "      - { edge: left, pins: 2, first: top, source: s1, page: 5 }\n" +
                    "      - { edge: left, pins: 2, first: top, source: s1, page: 5 }",
            ) + "  - { id: extra, header: j1, row: 1, index: 4, board_label: X, direction: nc, safe: ok, " +
                "cites: [{ source: s1, page: 5 }] }\n"
        }
        assertProblem(fixture.problems(), pins, line(), "two runs on the left edge need different orders")
    }

    @Test
    fun aRunCitationIsChecked() {
        edges("      - { edge: left, pins: 3, first: top, source: s9, page: 5 }")
        assertProblem(
            fixture.problems(),
            pins,
            fixture.lineOf(pins, "source: s9"),
            "source \"s9\" is not in sources.yaml",
        )
    }
}
