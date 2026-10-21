package com.example.flosshabit.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 10, 16)
    private fun daysAgo(n: Long) = today.minusDays(n)

    @Test
    fun noLogsGivesZero() {
        assertEquals(Streak(0, 0, false), StreakCalculator.calculate(emptyList(), today))
    }

    @Test
    fun streakIncludesToday() {
        val s = StreakCalculator.calculate(listOf(daysAgo(0), daysAgo(1), daysAgo(2)), today)
        assertEquals(Streak(3, 3, true), s)
    }

    @Test
    fun streakNotBrokenBeforeTodayIsDone() {
        val s = StreakCalculator.calculate(listOf(daysAgo(1), daysAgo(2)), today)
        assertEquals(Streak(2, 2, false), s)
    }

    @Test
    fun missedYesterdayBreaksStreak() {
        val s = StreakCalculator.calculate(listOf(daysAgo(2), daysAgo(3)), today)
        assertEquals(Streak(0, 2, false), s)
    }

    @Test
    fun longestKeepsEarlierRun() {
        val dates = listOf(10L, 9, 8, 7, 5, 1, 0).map(::daysAgo)
        assertEquals(Streak(2, 4, true), StreakCalculator.calculate(dates, today))
    }

    @Test
    fun multipleLogsOnOneDayCountOnce() {
        val s = StreakCalculator.calculate(listOf(today, today, today, daysAgo(1)), today)
        assertEquals(Streak(2, 2, true), s)
    }

    @Test
    fun futureDatesAreIgnored() {
        val s = StreakCalculator.calculate(listOf(today.plusDays(1), today), today)
        assertEquals(Streak(1, 1, true), s)
    }

    @Test
    fun streaksAreSeparatePerHabit() {
        val zone = ZoneId.of("Europe/Stockholm")
        val logs = listOf(
            HabitLog.create(Habit.FLOSS, Instant.parse("2026-10-16T18:00:00Z"), zone),
            HabitLog.create(Habit.FLOSS, Instant.parse("2026-10-15T18:00:00Z"), zone),
            HabitLog.create(Habit.MOUTHWASH, Instant.parse("2026-10-14T18:00:00Z"), zone),
        )
        assertEquals(Streak(2, 2, true), StreakCalculator.calculate(logs, Habit.FLOSS, today))
        assertEquals(Streak(0, 1, false), StreakCalculator.calculate(logs, Habit.MOUTHWASH, today))
    }

    @Test
    fun logJustAfterMidnightCountsForNewDay() {
        val zone = ZoneId.of("Europe/Stockholm")
        // 00:05 local on 16 Oct (CEST, UTC+2) is 22:05 UTC on 15 Oct.
        val log = HabitLog.create(Habit.FLOSS, Instant.parse("2026-10-15T22:05:00Z"), zone)
        assertEquals(today, log.localDate)
    }

    @Test
    fun dstChangeDoesNotBreakStreak() {
        val zone = ZoneId.of("Europe/Stockholm")
        // Clocks go back on 25 Oct 2026; log at 22:00 local each evening around it.
        val logs = listOf(
            "2026-10-24T20:00:00Z", // CEST
            "2026-10-25T21:00:00Z", // CET
            "2026-10-26T21:00:00Z",
        ).map { HabitLog.create(Habit.FLOSS, Instant.parse(it), zone) }
        val s = StreakCalculator.calculate(logs, Habit.FLOSS, LocalDate.of(2026, 10, 26))
        assertEquals(Streak(3, 3, true), s)
    }

    @Test
    fun timeZoneChangeKeepsStoredDates() {
        // Logged in Stockholm, then the user travels to New York.
        val stockholm = ZoneId.of("Europe/Stockholm")
        val newYork = ZoneId.of("America/New_York")
        val logs = listOf(
            HabitLog.create(Habit.FLOSS, Instant.parse("2026-10-15T05:30:00Z"), stockholm),
            HabitLog.create(Habit.FLOSS, Instant.parse("2026-10-16T23:00:00Z"), newYork),
        )
        assertEquals(LocalDate.of(2026, 10, 15), logs[0].localDate)
        assertEquals(LocalDate.of(2026, 10, 16), logs[1].localDate)
        assertEquals(Streak(2, 2, true), StreakCalculator.calculate(logs, Habit.FLOSS, today))
    }
}
