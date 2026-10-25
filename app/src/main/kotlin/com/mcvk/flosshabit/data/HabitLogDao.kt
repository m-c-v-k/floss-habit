package com.mcvk.flosshabit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Insert
    suspend fun insert(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_log WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM habit_log WHERE habit = :habit ORDER BY timestamp")
    fun observeByHabit(habit: String): Flow<List<HabitLogEntity>>
}
