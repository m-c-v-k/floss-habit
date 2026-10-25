package com.mcvk.flosshabit

import android.app.Application
import com.mcvk.flosshabit.data.FlossHabitDatabase
import com.mcvk.flosshabit.data.HabitLogRepository
import com.mcvk.flosshabit.data.RoomHabitLogRepository

/** Creates the app's shared objects by hand (no DI library, see docs/DECISIONS.md). */
class FlossHabitApp : Application() {
    private val database by lazy { FlossHabitDatabase.create(this) }

    val habitLogRepository: HabitLogRepository by lazy {
        RoomHabitLogRepository(database.habitLogDao())
    }
}
