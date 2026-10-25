package com.mcvk.flosshabit.data

import com.mcvk.flosshabit.core.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class RoomHabitLogRepositoryTest {

    private val dao = FakeHabitLogDao()
    private val stockholm = ZoneId.of("Europe/Stockholm")

    private fun repositoryAt(instant: String, zone: () -> ZoneId = { stockholm }) =
        RoomHabitLogRepository(dao, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC), zone)

    @Test
    fun logIsDatedInTheCurrentZone() = runBlocking {
        // 23:30 UTC on 24 Oct is 01:30 on 25 Oct in Stockholm.
        val log = repositoryAt("2026-10-24T23:30:00Z").log(Habit.FLOSS)
        assertEquals(LocalDate.of(2026, 10, 25), log.localDate)
        assertEquals(1L, log.id)
    }

    @Test
    fun zoneIsReadAtEachLog() = runBlocking {
        var zone = stockholm
        val repository = repositoryAt("2026-10-24T23:30:00Z") { zone }
        repository.log(Habit.FLOSS)
        zone = ZoneId.of("America/New_York") // travelled: still 24 Oct there
        val second = repository.log(Habit.FLOSS)
        assertEquals(LocalDate.of(2026, 10, 24), second.localDate)
    }

    @Test
    fun observeReturnsOnlyThatHabit() = runBlocking {
        val repository = repositoryAt("2026-10-21T08:00:00Z")
        repository.log(Habit.FLOSS)
        repository.log(Habit.MOUTHWASH)
        repository.log(Habit.FLOSS)
        assertEquals(2, repository.observeLogs(Habit.FLOSS).first().size)
        assertEquals(listOf(Habit.MOUTHWASH), repository.observeLogs(Habit.MOUTHWASH).first().map { it.habit })
    }

    @Test
    fun deleteRemovesOnceAndReportsMissing() = runBlocking {
        val repository = repositoryAt("2026-10-21T08:00:00Z")
        val log = repository.log(Habit.FLOSS)
        assertTrue(repository.delete(log.id!!))
        assertFalse(repository.delete(log.id!!))
        assertTrue(repository.observeLogs(Habit.FLOSS).first().isEmpty())
    }
}

/** In-memory stand-in for the Room DAO; the real queries are covered by HabitLogDaoTest. */
private class FakeHabitLogDao : HabitLogDao {
    private val rows = MutableStateFlow(emptyList<HabitLogEntity>())
    private var nextId = 1L

    override suspend fun insert(log: HabitLogEntity): Long {
        val id = nextId++
        rows.value = rows.value + log.copy(id = id)
        return id
    }

    override suspend fun deleteById(id: Long): Int {
        val before = rows.value.size
        rows.value = rows.value.filterNot { it.id == id }
        return before - rows.value.size
    }

    override fun observeByHabit(habit: String): Flow<List<HabitLogEntity>> =
        rows.map { all -> all.filter { it.habit == habit }.sortedBy { it.timestamp } }
}
