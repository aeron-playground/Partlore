package dev.partlore.tools.importer.rst

import org.junit.Assert.assertEquals
import org.junit.Test

// Made-up notes in the shapes the guides use. Not copied from any guide.
class GuideNotesTest {
    @Test
    fun gpiosNamedInNotesAndFootnotesAreFoundWithTheirLine() {
        val rst =
            """
            Intro text that names GPIO1 outside any note.

            .. note::

                On some modules GPIO35 and GPIO36 are used inside the module.

            .. warning:: GPIO7 can't be used.

            Text again.

            .. [2] GPIO2 and GPIO8 choose how the chip starts. RTC_GPIO9 is not a GPIO name.
            """.trimIndent()
        assertEquals(
            mapOf(
                "GPIO2" to listOf(11),
                "GPIO35" to listOf(3),
                "GPIO36" to listOf(3),
                "GPIO7" to listOf(7),
                "GPIO8" to listOf(11),
            ),
            GuideNotes.pinsNamed(rst),
        )
    }
}
