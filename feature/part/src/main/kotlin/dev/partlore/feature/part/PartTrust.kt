package dev.partlore.feature.part

import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.PartPage

/** The status the page leads with: its least checked file, so it never claims more than its weakest part. */
internal fun PartPage.weakestStatus(): FileStatus = listOfNotNull(partStatus, pinsStatus, gotchasStatus)
    .minWith(compareBy<FileStatus> { it.level.trust }.thenBy { it.checkedBy.size })
