package dev.partlore.tools.content.rules

import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD_DIR
import dev.partlore.tools.content.testing.assertProblem
import dev.partlore.tools.content.testing.problems
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SafetyAndLicenceRulesTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var fixture: ContentFixture
    private val part = "$BOARD_DIR/part.yaml"
    private val pins = "$BOARD_DIR/pins.yaml"
    private val sources = "$BOARD_DIR/sources.yaml"

    @Before
    fun setUp() {
        fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
    }

    @Test
    fun aStrappingPinCantBeMarkedSafe() {
        fixture.edit(pins) { it.replace("safe: caution", "safe: ok") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: gpio0"), "strapping pin")
    }

    @Test
    fun aGroundPinCantHaveFunctions() {
        fixture.edit(pins) {
            it.replace("direction: ground\n", "direction: ground\n    functions:\n      - { type: gpio }\n")
        }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: gnd"), "can't have functions")
    }

    @Test
    fun anIoPinNeedsAVoltage() {
        fixture.edit(pins) { it.replaceFirst("    voltage: 3.3\n", "") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: gpio1"), "needs a voltage")
    }

    @Test
    fun anIoPinNeedsFiveVoltTolerance() {
        fixture.edit(pins) { it.replaceFirst("    five_v_tolerant: false\n", "") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: gpio1"), "needs five_v_tolerant")
    }

    @Test
    fun aPinNeedsAName() {
        fixture.edit(pins) { it.replace("    board_label: GND\n", "") }
        assertProblem(fixture.problems(), pins, fixture.lineOf(pins, "- id: gnd"), "needs at least one name")
    }

    @Test
    fun minCantBeAboveMax() {
        fixture.edit(part) { it.replace("min: 3.0, typ: 3.3", "min: 3.9, typ: 3.3") }
        assertProblem(fixture.problems(), part, fixture.lineOf(part, "min: 3.9"), "min 3.9 is above max 3.6")
    }

    @Test
    fun aNonCommercialLicenceIsRejectedWithTheReason() {
        fixture.edit(sources) { it.replace("license: CC-BY-SA-4.0", "license: CC-BY-NC-SA-4.0") }
        assertProblem(fixture.problems(), sources, fixture.lineOf(sources, "id: s2"), "doesn't allow commercial use")
    }

    @Test
    fun anUnknownLicenceIsRejected() {
        fixture.edit(sources) { it.replace("license: CC-BY-SA-4.0", "license: GPL-3.0") }
        assertProblem(fixture.problems(), sources, fixture.lineOf(sources, "id: s2"), "not on the allow-list")
    }

    @Test
    fun aPdfSourceNeedsVersionAndHash() {
        fixture.edit(sources) { it.replace(Regex("    sha256: \"0+\"\n"), "") }
        assertProblem(fixture.problems(), sources, fixture.lineOf(sources, "id: s1"), "add version and the sha256")
    }
}
