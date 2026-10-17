package com.example.flosshabit.core

import java.time.LocalDate

data class Streak(
    val current: Int,
    val longest: Int,
    val doneToday: Boolean,
)

object StreakCalculator {

    /** Streaks for one habit; logs for other habits are ignored. */
    fun calculate(logs: Iterable<HabitLog>, habit: Habit, today: LocalDate): Streak =
        calculate(logs.filter { it.habit == habit }.map { it.localDate }, today)

    /**
     * A day counts if it has at least one log. The current streak ends today,
     * or yesterday if today is not done yet, so it isn't shown as broken
     * before the day is over. Dates after [today] are ignored.
     */
    fun calculate(dates: Iterable<LocalDate>, today: LocalDate): Streak {
        val days = dates.filter { !it.isAfter(today) }.toSortedSet()
        val doneToday = today in days

        var current = 0
        var day = if (doneToday) today else today.minusDays(1)
        while (day in days) {
            current++
            day = day.minusDays(1)
        }

        var longest = 0
        var run = 0
        var previous: LocalDate? = null
        for (d in days) {
            run = if (previous != null && previous.plusDays(1) == d) run + 1 else 1
            longest = maxOf(longest, run)
            previous = d
        }

        return Streak(current = current, longest = longest, doneToday = doneToday)
    }
}
