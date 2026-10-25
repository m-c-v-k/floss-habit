package com.mcvk.flosshabit.data

import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class HabitLogMappingTest {

    @Test
    fun storedColumnsAreReadableValues() {
        val log = HabitLog(Habit.MOUTHWASH, Instant.parse("2026-10-21T19:30:00Z"), LocalDate.of(2026, 10, 21))
        val row = log.toEntity()
        assertEquals(0L, row.id) // not stored yet: Room generates the id
        assertEquals("MOUTHWASH", row.habit)
        assertEquals(1_792_611_000_000L, row.timestamp)
        assertEquals("2026-10-21", row.localDate)
    }

    @Test
    fun roundTripKeepsEveryField() {
        val log = HabitLog(Habit.FLOSS, Instant.parse("2026-03-29T00:15:00.123Z"), LocalDate.of(2026, 3, 29), id = 42)
        assertEquals(log, log.toEntity().toDomain())
    }

    @Test
    fun localDateIsNotRecalculatedFromTimestamp() {
        // Logged just after midnight in Stockholm: UTC is still the previous day.
        val row = HabitLogEntity(id = 1, habit = "FLOSS", timestamp = Instant.parse("2026-10-20T22:30:00Z").toEpochMilli(), localDate = "2026-10-21")
        assertEquals(LocalDate.of(2026, 10, 21), row.toDomain().localDate)
    }
}
