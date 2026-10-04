package dev.partlore.tools.content.checklist

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.content.testing.TODAY
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ChecklistTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun checklist(): String {
        val fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
        return Checklist.render(Validator(fixture.root, TODAY).validate(Mode.PREVIEW).content.parts.single())
    }

    private fun assertInOrder(text: String, vararg parts: String) {
        var from = 0
        parts.forEach { part ->
            val at = text.indexOf(part, from)
            assertTrue("\"$part\" missing or out of order in:\n$text", at >= 0)
            from = at + part.length
        }
    }

    @Test
    fun valuesAreGroupedBySourceThenPage() {
        assertInOrder(
            checklist(),
            "# Checklist: Test Board (testmaker/test-board)",
            "## s1: Test Board Datasheet (1.0)",
            "### Page 2",
            "spec supply.voltage: min 3, typ 3.3, max 3.6 V",
            "### Page 3",
            "absolute max supply.voltage: max 3.9 V",
            "### Page 4",
            "I²C address 0x3c (default), ADDR to GND",
            "### Page 5",
            "header j1 \"J1\": header, 1 × 3, pitch 2.54 mm",
            "pin gpio1",
            "### Page 6",
            "gotcha boot-pin (caution): GPIO0 must be high at reset",
            "## s2: Test Board Guide",
            "### Section “Radio”",
            "spec radio.wifi: value 802.11 b/g/n",
        )
    }

    @Test
    fun aStrappingPinWithoutALevelReadsCleanly() {
        val fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
        fixture.edit("parts/testmaker/test-board/pins.yaml") { it.replace("must_be: high, ", "") }
        val text = Checklist.render(Validator(fixture.root, TODAY).validate(Mode.PREVIEW).content.parts.single())
        assertTrue(text, text.contains("strapping boot-mode at reset"))
    }

    @Test
    fun everyLineIsATickBoxWithItsFileAndLine() {
        assertTrue(checklist().contains("- [ ] `pins.yaml:"))
    }
}
