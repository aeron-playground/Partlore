package dev.partlore.tools.content.rules

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

// UTC+14 is the time zone furthest ahead.
private const val FURTHEST_AHEAD_HOURS = 14

/** Today where it is latest on Earth, so a check made today anywhere is never "in the future" on CI. */
fun latestToday(clock: Clock = Clock.systemUTC()): LocalDate =
    LocalDate.now(clock.withZone(ZoneOffset.ofHours(FURTHEST_AHEAD_HOURS)))
