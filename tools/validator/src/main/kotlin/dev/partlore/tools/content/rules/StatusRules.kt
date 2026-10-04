package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Level
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Status
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.ship.Shipping
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Each status level has its required fields; a released part only points at released parts. */
class StatusRules(private val today: LocalDate) {
    fun check(content: Content, mode: Mode, d: Diagnostics) {
        content.parts.forEach { part ->
            listOfNotNull(part.status, part.pins?.status, part.gotchas?.status).forEach { status ->
                problems(status).forEach { d.error(status.pos, it) }
            }
            part.gotchas?.status?.takeIf { it.level == Level.IMPORTED }?.let {
                d.error(it.pos, "gotchas are written by people, so gotchas.yaml can't be imported")
            }
        }
        if (mode == Mode.RELEASE) {
            content.parts.filter { Shipping.partShips(it, mode) }.forEach { releaseLinks(it, content, d) }
        }
    }

    private fun problems(status: Status): List<String> = when (status.level) {
        Level.CHECKED, Level.VERIFIED -> checkProblems(status)

        Level.IMPORTED -> listOfNotNull("imported needs imported_from".takeIf { status.importedFrom == null })

        Level.NEEDS_REVIEW, Level.DISPUTED ->
            listOfNotNull("${status.level.yaml} needs a note saying what is wrong".takeIf { status.note == null })

        Level.DRAFT -> emptyList()
    } + listOfNotNull(
        "imported_from only belongs with level: imported".takeIf {
            status.importedFrom != null && status.level != Level.IMPORTED
        },
    )

    private fun checkProblems(status: Status): List<String> = buildList {
        val level = status.level.yaml
        if (status.checkedBy.isEmpty()) add("$level needs checked_by")
        if (status.against.isEmpty()) add("$level needs against")
        if (status.level == Level.VERIFIED && status.checkedBy.size < 2) {
            add("verified needs two different people in checked_by")
        }
        val date = status.checkedOn
        val parsed = date?.let(::parse)
        when {
            date == null -> add("$level needs checked_on")
            parsed == null -> add("checked_on $date is not a real date")
            parsed > today -> add("checked_on $date is in the future")
        }
    }

    private fun parse(date: String): LocalDate? = try {
        LocalDate.parse(date)
    } catch (_: DateTimeParseException) {
        null
    }

    private fun releaseLinks(part: Part, content: Content, d: Diagnostics) {
        val parts = content.parts.associateBy { it.id }
        val targets = listOfNotNull(part.uses?.let { "uses" to it }) + part.related.map { "related" to it }
        targets.mapNotNull { (field, id) -> parts[id]?.let { field to it } }
            .filterNot { (_, target) -> Shipping.partShips(target, Mode.RELEASE) }
            .forEach { (field, target) ->
                d.error(
                    part.at(field),
                    "${target.id} isn't in release packs yet (status ${target.status.level.yaml}), " +
                        "so ${part.id} can't point to it",
                )
            }
        // Gotchas that ship while their pins don't would point at pins the app doesn't have.
        val pinsMissing = Shipping.gotchasShip(part, Mode.RELEASE) && !Shipping.pinsShip(part, Mode.RELEASE)
        part.gotchas?.takeIf { pinsMissing && it.gotchas.any { gotcha -> gotcha.pins.isNotEmpty() } }?.let {
            d.error(it.status.pos, "gotchas name pins, but pins.yaml isn't checked yet")
        }
    }
}
