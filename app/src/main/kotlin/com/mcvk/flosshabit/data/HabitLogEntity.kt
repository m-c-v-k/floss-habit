package com.mcvk.flosshabit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import java.time.Instant
import java.time.LocalDate

/**
 * One row per log. [habit] is the [Habit] name, [timestamp] is epoch millis (UTC)
 * and [localDate] is ISO text ("2026-10-21"), which sorts in date order.
 */
@Entity(
    tableName = "habit_log",
    indices = [Index(value = ["habit", "local_date"])],
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habit: String,
    val timestamp: Long,
    @ColumnInfo(name = "local_date") val localDate: String,
)

fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    habit = Habit.valueOf(habit),
    timestamp = Instant.ofEpochMilli(timestamp),
    localDate = LocalDate.parse(localDate),
    id = id,
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id = id ?: 0,
    habit = habit.name,
    timestamp = timestamp.toEpochMilli(),
    localDate = localDate.toString(),
)
