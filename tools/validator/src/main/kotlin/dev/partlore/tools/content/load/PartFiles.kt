package dev.partlore.tools.content.load

import dev.partlore.tools.content.model.GotchaFile
import dev.partlore.tools.content.model.PinFile
import dev.partlore.tools.content.model.Source

/** The other files of a part, already mapped. */
internal data class PartFiles(
    val sources: List<Source>,
    val pins: PinFile?,
    val gotchas: GotchaFile?,
    val article: String?,
)
