package dev.partlore.tools.content.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TodayTest {
    // CI runs in UTC; a maintainer in Australia is already a day ahead in the morning.
    @Test
    fun todayIsTheDateWhereItIsLatest() {
        val clock = Clock.fixed(Instant.parse("2026-10-04T22:00:00Z"), ZoneOffset.UTC)
        assertEquals(LocalDate.of(2026, 10, 5), latestToday(clock))
    }
}
