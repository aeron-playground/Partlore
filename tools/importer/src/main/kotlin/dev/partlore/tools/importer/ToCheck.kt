package dev.partlore.tools.importer

import dev.partlore.tools.importer.Importer.Pin

/** What the person checking an imported board must look at: things the tables can't settle. */
internal object ToCheck {
    fun list(pins: List<Pin>, notes: Map<String, List<Int>>): List<String> = listOf(
        "Board labels: each board label is the guide's Name unless a hint sets it. Compare them with the board photo.",
    ) +
        pins.flatMap { pin ->
            val where = "${pin.table.heading} No. ${pin.row.no} (${pin.chipName ?: pin.row.name})"
            pin.row.footnotes.sorted().map {
                "$where: the guide has footnote $it here. Read it; put what matters in the hints."
            } +
                pin.facts.flags.map { "$where: '$it' is not a pin function. Say it in a note if it matters." } +
                noted(pin, notes, where)
        }

    // A note can say a pin is taken or risky without marking its row, so every note that names it counts.
    private fun noted(pin: Pin, notes: Map<String, List<Int>>, where: String): List<String> {
        val lines = pin.facts.chipName?.let { notes[it] } ?: return emptyList()
        return listOf(
            "$where: the guide names ${pin.facts.chipName} in a note at line ${lines.joinToString()}. Read it; " +
                "put what matters in the hints.",
        )
    }
}
