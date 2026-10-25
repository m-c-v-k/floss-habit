package com.mcvk.flosshabit.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mcvk.flosshabit.core.Habit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class HabitLogDaoTest {
    private lateinit var db: FlossHabitDatabase
    private lateinit var dao: HabitLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, FlossHabitDatabase::class.java).build()
        dao = db.habitLogDao()
    }

    @After fun tearDown() = db.close()

    private fun row(habit: String, timestamp: String, localDate: String) =
        HabitLogEntity(habit = habit, timestamp = Instant.parse(timestamp).toEpochMilli(), localDate = localDate)

    @Test
    fun observeFiltersByHabitAndSortsByTime() = runBlocking {
        dao.insert(row("FLOSS", "2026-10-21T20:00:00Z", "2026-10-21"))
        dao.insert(row("MOUTHWASH", "2026-10-21T20:05:00Z", "2026-10-21"))
        dao.insert(row("FLOSS", "2026-10-20T20:00:00Z", "2026-10-20"))
        val floss = dao.observeByHabit("FLOSS").first()
        assertEquals(listOf("2026-10-20", "2026-10-21"), floss.map { it.localDate })
        assertEquals(1, dao.observeByHabit("MOUTHWASH").first().size)
    }

    @Test
    fun insertGeneratesIdsAndDeleteRemovesOne() = runBlocking {
        val first = dao.insert(row("FLOSS", "2026-10-21T20:00:00Z", "2026-10-21"))
        val second = dao.insert(row("FLOSS", "2026-10-21T21:00:00Z", "2026-10-21"))
        assertTrue(second > first)
        assertEquals(1, dao.deleteById(second))
        assertEquals(0, dao.deleteById(second))
        assertEquals(listOf(first), dao.observeByHabit("FLOSS").first().map { it.id })
    }

    @Test
    fun repositoryRoundTripThroughRoom() = runBlocking {
        val repository = RoomHabitLogRepository(
            dao,
            Clock.fixed(Instant.parse("2026-10-24T23:30:00Z"), ZoneOffset.UTC),
        ) { ZoneId.of("Europe/Stockholm") }
        val stored = repository.log(Habit.MOUTHWASH)
        val read = repository.observeLogs(Habit.MOUTHWASH).first().single()
        assertEquals(stored, read)
        assertEquals(LocalDate.of(2026, 10, 25), read.localDate)
    }
}
