package dev.partlore.feature.part

import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Test

class PartTrustTest {
    private val checkedByOne = FileStatus(VerificationLevel.Checked, listOf("a"), "2026-10-04", listOf("s1"), null)
    private val draft = FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null)

    @Test
    fun theLeastCheckedFileWins() {
        val page = SampleContent.devBoard.copy(
            partStatus = checkedByOne,
            pinsStatus = draft,
            gotchasStatus = checkedByOne,
        )
        assertEquals(VerificationLevel.Draft, page.weakestStatus().level)
    }

    @Test
    fun amongCheckedFilesTheFewestCheckersWin() {
        val byTwo = checkedByOne.copy(checkedBy = listOf("a", "b"))
        val page = SampleContent.devBoard.copy(partStatus = byTwo, pinsStatus = checkedByOne, gotchasStatus = byTwo)
        assertEquals(checkedByOne, page.weakestStatus())
    }

    @Test
    fun disputedOutranksEverything() {
        val disputed = draft.copy(level = VerificationLevel.Disputed)
        val page = SampleContent.devBoard.copy(partStatus = draft, pinsStatus = null, gotchasStatus = disputed)
        assertEquals(VerificationLevel.Disputed, page.weakestStatus().level)
    }
}
