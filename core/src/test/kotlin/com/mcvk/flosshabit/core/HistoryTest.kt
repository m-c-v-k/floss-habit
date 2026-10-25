package com.mcvk.flosshabit.core

import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HistoryTest {

    private val today = LocalDate.of(2026, 10, 25) // a Sunday

    private fun log(habit: Habit, date: LocalDate) = HabitLog(habit, Instant.EPOCH, date)

    @Test
    fun weekIsTheLastSevenDays() {
        val period = History.period(HistoryRange.WEEK, today, firstLog = null)
        assertEquals(LocalDate.of(2026, 10, 19)..today, period)
    }

    @Test
    fun monthIsTheCalendarMonthUpToToday() {
        val period = History.period(HistoryRange.MONTH, today, firstLog = null)
        assertEquals(LocalDate.of(2026, 10, 1)..today, period)
    }

    @Test
    fun allTimeStartsAtTheFirstLog() {
        val first = LocalDate.of(2026, 3, 2)
        assertEquals(first..today, History.period(HistoryRange.ALL_TIME, today, first))
    }

    @Test
    fun allTimeShowsAtLeastAWeek() {
        val week = LocalDate.of(2026, 10, 19)..today
        assertEquals(week, History.period(HistoryRange.ALL_TIME, today, firstLog = null))
        assertEquals(week, History.period(HistoryRange.ALL_TIME, today, firstLog = today.minusDays(2)))
    }

    @Test
    fun dailyCountsCountEveryLogAndFillEmptyDays() {
        val logs = listOf(
            log(Habit.FLOSS, today),
            log(Habit.FLOSS, today),
            log(Habit.FLOSS, today.minusDays(2)),
            log(Habit.MOUTHWASH, today.minusDays(1)), // other habit
            log(Habit.FLOSS, today.minusDays(10)), // before the period
        )
        val counts = History.dailyCounts(logs, Habit.FLOSS, today.minusDays(3)..today)
        assertEquals(listOf(0, 1, 0, 2), counts.map { it.count })
        assertEquals(today.minusDays(3), counts.first().date)
        assertEquals(today, counts.last().date)
    }

    @Test
    fun totalDaysCountsEachDayOnceAndIgnoresTheFuture() {
        val logs = listOf(
            log(Habit.FLOSS, today),
            log(Habit.FLOSS, today),
            log(Habit.FLOSS, LocalDate.of(2025, 1, 1)),
            log(Habit.FLOSS, today.plusDays(1)),
            log(Habit.MOUTHWASH, today.minusDays(1)),
        )
        assertEquals(2, History.totalDays(logs, Habit.FLOSS, today))
        assertEquals(1, History.totalDays(logs, Habit.MOUTHWASH, today))
    }

    @Test
    fun weeksStartOnMondayWithPadding() {
        // 1 Oct 2026 is a Thursday; 25 Oct a Sunday.
        val days = History.dailyCounts(emptyList(), Habit.FLOSS, LocalDate.of(2026, 10, 1)..today)
        val weeks = History.weeks(days)
        assertEquals(4, weeks.size)
        assertTrue(weeks.all { it.size == 7 })
        assertEquals(listOf(null, null, null), weeks.first().take(3))
        assertEquals(LocalDate.of(2026, 10, 1), weeks.first()[3]?.date)
        assertEquals(today, weeks.last().last()?.date)
    }

    @Test
    fun weeksPadTheEndOfAPartialWeek() {
        val wednesday = LocalDate.of(2026, 10, 21)
        val weeks = History.weeks(History.dailyCounts(emptyList(), Habit.FLOSS, wednesday..wednesday))
        assertEquals(1, weeks.size)
        assertEquals(wednesday, weeks.single()[2]?.date)
        assertNull(weeks.single()[3])
    }
}
