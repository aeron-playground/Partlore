package dev.partlore.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentModelTest {
    @Test
    fun packNamesMapToLevels() {
        assertEquals(VerificationLevel.NeedsReview, VerificationLevel.fromPack("needs-review"))
        assertEquals(VerificationLevel.Verified, VerificationLevel.fromPack("verified"))
    }

    @Test
    fun unknownLevelsCountAsDraft() {
        assertEquals(VerificationLevel.Draft, VerificationLevel.fromPack("triple-checked"))
        assertEquals(VerificationLevel.Draft, VerificationLevel.fromPack(null))
    }

    @Test
    fun theWeakestLevelWins() {
        val levels = listOf(VerificationLevel.Verified, VerificationLevel.Disputed, VerificationLevel.Checked)
        assertEquals(VerificationLevel.Disputed, VerificationLevel.weakestOf(levels))
        assertEquals(
            VerificationLevel.Draft,
            VerificationLevel.weakestOf(listOf(VerificationLevel.Checked, VerificationLevel.Draft)),
        )
        assertEquals(VerificationLevel.Draft, VerificationLevel.weakestOf(emptyList()))
    }

    @Test
    fun unknownSeveritiesShowAsCaution() {
        assertEquals(Severity.Danger, Severity.fromPack("danger"))
        assertEquals(Severity.Caution, Severity.fromPack("critical"))
    }

    @Test
    fun fiveVoltToleranceIsNullWhenTheGlanceHasNone() {
        assertEquals(FiveVoltTolerance.Some, FiveVoltTolerance.fromPack("some"))
        assertNull(FiveVoltTolerance.fromPack(null))
    }

    @Test
    fun dangerCountCountsOnlyDangerGotchas() {
        val gotcha = GotchaItem("a", Severity.Danger, "t", "b", emptyList(), emptyList())
        val page = samplePage(listOf(gotcha, gotcha.copy(id = "b", severity = Severity.Info), gotcha.copy(id = "c")))
        assertEquals(2, page.dangerCount)
    }

    private fun samplePage(gotchas: List<GotchaItem>) = PartPage(
        id = "maker/part",
        name = "Part",
        aliases = emptyList(),
        kind = "board",
        manufacturer = "Maker",
        summary = "A part.",
        uses = null,
        usedBy = emptyList(),
        related = emptyList(),
        glance = Glance(null, null, null, null, null, null),
        specs = emptyList(),
        absoluteMax = emptyList(),
        i2c = emptyList(),
        gotchas = gotchas,
        sources = emptyList(),
        partStatus = FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null),
        pinsStatus = null,
        gotchasStatus = null,
        packVersion = "0.1.0",
    )
}
