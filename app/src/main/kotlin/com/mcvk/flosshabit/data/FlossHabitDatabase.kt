package com.mcvk.flosshabit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** The single source of truth shared by the app, widgets and notification actions. */
@Database(entities = [HabitLogEntity::class], version = 1, exportSchema = true)
abstract class FlossHabitDatabase : RoomDatabase() {
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        fun create(context: Context): FlossHabitDatabase =
            Room.databaseBuilder(context, FlossHabitDatabase::class.java, "floss-habit.db").build()
    }
}
