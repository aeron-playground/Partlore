package dev.partlore.feature.part

import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.VerificationLevel

/** From least to most trustworthy. */
private val TRUST_ORDER =
    listOf(
        VerificationLevel.Disputed,
        VerificationLevel.NeedsReview,
        VerificationLevel.Draft,
        VerificationLevel.Imported,
        VerificationLevel.Checked,
        VerificationLevel.Verified,
    )

/** The status the page leads with: its least checked file, so it never claims more than its weakest part. */
internal fun PartPage.weakestStatus(): FileStatus = listOfNotNull(partStatus, pinsStatus, gotchasStatus)
    .minWith(compareBy<FileStatus> { TRUST_ORDER.indexOf(it.level) }.thenBy { it.checkedBy.size })
