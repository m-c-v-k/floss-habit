package com.mcvk.flosshabit.data

import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.ZoneId

/** Storage for habit logs; the one seam between features and the database. */
interface HabitLogRepository {
    /** All logs for [habit], oldest first; emits again after every change. */
    fun observeLogs(habit: Habit): Flow<List<HabitLog>>

    /** Logs [habit] now, dated in the current time zone. Returns the stored log, with its id. */
    suspend fun log(habit: Habit): HabitLog

    /** Removes one log, for undo. Returns false if it was already gone. */
    suspend fun delete(id: Long): Boolean
}

/**
 * [zone] is read at each log, so a time zone change (travel) applies to the next log
 * without restarting the app.
 */
class RoomHabitLogRepository(
    private val dao: HabitLogDao,
    private val clock: Clock = Clock.systemUTC(),
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) : HabitLogRepository {

    override fun observeLogs(habit: Habit): Flow<List<HabitLog>> =
        dao.observeByHabit(habit.name).map { rows -> rows.map { it.toDomain() } }

    override suspend fun log(habit: Habit): HabitLog {
        val log = HabitLog.create(habit, clock.instant(), zone())
        val id = dao.insert(log.toEntity())
        return log.copy(id = id)
    }

    override suspend fun delete(id: Long): Boolean = dao.deleteById(id) > 0
}
