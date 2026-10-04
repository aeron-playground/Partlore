package dev.partlore.tools.content.ship

import dev.partlore.tools.content.model.Level
import dev.partlore.tools.content.model.Part

/** RELEASE: what users get. PREVIEW: everything, drafts too, for checking pull requests. */
enum class Mode { RELEASE, PREVIEW }

/** What goes into a pack. Imported parts ship; their pins and gotchas wait for a person. */
object Shipping {
    private val PART_LEVELS = setOf(Level.IMPORTED, Level.CHECKED, Level.VERIFIED, Level.NEEDS_REVIEW, Level.DISPUTED)
    private val CHECKED_LEVELS = setOf(Level.CHECKED, Level.VERIFIED, Level.NEEDS_REVIEW, Level.DISPUTED)

    fun partShips(part: Part, mode: Mode): Boolean = mode == Mode.PREVIEW || part.status.level in PART_LEVELS

    fun pinsShip(part: Part, mode: Mode): Boolean {
        val level = part.pins?.status?.level
        return level != null && partShips(part, mode) && (mode == Mode.PREVIEW || level in CHECKED_LEVELS)
    }

    fun gotchasShip(part: Part, mode: Mode): Boolean {
        val level = part.gotchas?.status?.level
        return level != null && partShips(part, mode) && (mode == Mode.PREVIEW || level in CHECKED_LEVELS)
    }

    fun articleShips(part: Part, mode: Mode): Boolean =
        part.article != null && partShips(part, mode) && (mode == Mode.PREVIEW || part.status.level in CHECKED_LEVELS)
}
