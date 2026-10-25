package com.mcvk.flosshabit.core

import java.time.DayOfWeek
import java.time.LocalDate

enum class HistoryRange { WEEK, MONTH, ALL_TIME }

/** How many times a habit was logged on [date]; 0 for a day without logs. */
data class DayCount(val date: LocalDate, val count: Int)

object History {

    /**
     * The days a [range] covers, always ending [today]:
     * - WEEK: the last 7 days.
     * - MONTH: the current calendar month up to today.
     * - ALL_TIME: from [firstLog] (or at least the last 7 days).
     */
    fun period(range: HistoryRange, today: LocalDate, firstLog: LocalDate?): ClosedRange<LocalDate> {
        val weekStart = today.minusDays(6)
        val start = when (range) {
            HistoryRange.WEEK -> weekStart
            HistoryRange.MONTH -> today.withDayOfMonth(1)
            HistoryRange.ALL_TIME -> firstLog?.takeIf { it.isBefore(weekStart) } ?: weekStart
        }
        return start..today
    }

    /** One entry per day in [period], oldest first; logs for other habits or outside it are ignored. */
    fun dailyCounts(logs: Iterable<HabitLog>, habit: Habit, period: ClosedRange<LocalDate>): List<DayCount> {
        val counts = logs
            .filter { it.habit == habit && it.localDate in period }
            .groupingBy { it.localDate }
            .eachCount()
        return generateSequence(period.start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(period.endInclusive) }
            .map { DayCount(it, counts[it] ?: 0) }
            .toList()
    }

    /** Days with at least one log for [habit], up to and including [today]. */
    fun totalDays(logs: Iterable<HabitLog>, habit: Habit, today: LocalDate): Int =
        logs.filter { it.habit == habit && !it.localDate.isAfter(today) }
            .map { it.localDate }
            .toSet()
            .size

    /**
     * Lays [days] out as calendar weeks, Monday first. Cells before the first day
     * and after the last are null, so every week has 7 entries.
     */
    fun weeks(days: List<DayCount>): List<List<DayCount?>> {
        if (days.isEmpty()) return emptyList()
        val leading = days.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value
        val trailing = DayOfWeek.SUNDAY.value - days.last().date.dayOfWeek.value
        val cells: List<DayCount?> = List(leading) { null } + days + List(trailing) { null }
        return cells.chunked(7)
    }
}
