package com.mcvk.flosshabit.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class Habit { FLOSS, MOUTHWASH }

/**
 * One logged habit. [localDate] is fixed at log time in the user's time zone,
 * so later time zone or DST changes never move a log to another day.
 * [id] is null until the log has been stored.
 */
data class HabitLog(
    val habit: Habit,
    val timestamp: Instant,
    val localDate: LocalDate,
    val id: Long? = null,
) {
    companion object {
        fun create(habit: Habit, timestamp: Instant, zone: ZoneId): HabitLog =
            HabitLog(habit, timestamp, timestamp.atZone(zone).toLocalDate())
    }
}
