package com.mcvk.flosshabit.dashboard

import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import com.mcvk.flosshabit.core.HistoryRange
import com.mcvk.flosshabit.data.HabitLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val today = LocalDate.of(2026, 10, 25)
    private val date = MutableStateFlow(today)
    private val repository = FakeHabitLogRepository { date.value }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(): DashboardViewModel {
        val viewModel = DashboardViewModel(repository, date)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun DashboardViewModel.card(habit: Habit) = uiState.value.cards.single { it.habit == habit }

    @Test
    fun startsEmptyForBothHabits() = runTest {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(Habit.FLOSS, Habit.MOUTHWASH), viewModel.uiState.value.cards.map { it.habit })
        assertEquals(0, viewModel.card(Habit.FLOSS).logsToday)
        assertEquals(0, viewModel.card(Habit.FLOSS).streak.current)
    }

    @Test
    fun severalLogsTodayCountAsOneStreakDay() = runTest {
        repository.add(Habit.FLOSS, today.minusDays(1))
        val viewModel = viewModel()
        viewModel.onDone(Habit.FLOSS)
        viewModel.onDone(Habit.FLOSS)
        val floss = viewModel.card(Habit.FLOSS)
        assertEquals(2, floss.logsToday)
        assertEquals(2, floss.streak.current)
        assertTrue(floss.streak.doneToday)
        assertEquals(0, viewModel.card(Habit.MOUTHWASH).logsToday)
    }

    @Test
    fun undoRemovesOnlyTheLastLog() = runTest {
        val viewModel = viewModel()
        viewModel.onDone(Habit.MOUTHWASH)
        viewModel.onDone(Habit.MOUTHWASH)
        assertEquals(Habit.MOUTHWASH, viewModel.uiState.value.undoable?.habit)
        viewModel.onUndo()
        assertEquals(1, viewModel.card(Habit.MOUTHWASH).logsToday)
        assertNull(viewModel.uiState.value.undoable)
        viewModel.onUndo() // nothing left to undo
        assertEquals(1, viewModel.card(Habit.MOUTHWASH).logsToday)
    }

    @Test
    fun dismissingKeepsTheLog() = runTest {
        val viewModel = viewModel()
        viewModel.onDone(Habit.FLOSS)
        viewModel.onUndoDismissed()
        assertNull(viewModel.uiState.value.undoable)
        assertEquals(1, viewModel.card(Habit.FLOSS).logsToday)
    }

    @Test
    fun newDayResetsTodayButKeepsTheStreak() = runTest {
        val viewModel = viewModel()
        viewModel.onDone(Habit.FLOSS)
        date.value = today.plusDays(1) // midnight passed
        val floss = viewModel.card(Habit.FLOSS)
        assertEquals(0, floss.logsToday)
        assertFalse(floss.streak.doneToday)
        assertEquals(1, floss.streak.current) // not broken until the new day is over
    }

    @Test
    fun historyShowsTheLastSevenDaysByDefault() = runTest {
        repository.add(Habit.FLOSS, today.minusDays(2))
        val viewModel = viewModel()
        viewModel.onDone(Habit.FLOSS)
        viewModel.onDone(Habit.FLOSS)
        val state = viewModel.uiState.value
        assertEquals(HistoryRange.WEEK, state.historyRange)
        val floss = state.history.single { it.habit == Habit.FLOSS }
        assertEquals(listOf(0, 0, 0, 0, 1, 0, 2), floss.days.map { it.count })
        assertEquals(today, floss.days.last().date)
        assertEquals(2, floss.totalDays)
        assertEquals(0, state.history.single { it.habit == Habit.MOUTHWASH }.totalDays)
    }

    @Test
    fun allTimeStartsAtTheEarliestLogOfEitherHabit() = runTest {
        val first = LocalDate.of(2026, 9, 1)
        repository.add(Habit.MOUTHWASH, first)
        repository.add(Habit.FLOSS, LocalDate.of(2026, 10, 1))
        val viewModel = viewModel()
        viewModel.onHistoryRangeSelected(HistoryRange.ALL_TIME)
        val state = viewModel.uiState.value
        assertEquals(HistoryRange.ALL_TIME, state.historyRange)
        state.history.forEach { habit ->
            assertEquals(first, habit.days.first().date)
            assertEquals(today, habit.days.last().date)
            assertTrue(habit.weeks.all { it.size == 7 })
        }
        viewModel.onHistoryRangeSelected(HistoryRange.MONTH)
        assertEquals(LocalDate.of(2026, 10, 1), viewModel.uiState.value.history.first().days.first().date)
    }

    @Test
    fun currentDateFollowsClockAndZone() = runTest {
        var zone: ZoneId = ZoneId.of("Europe/Stockholm")
        val clock = Clock.fixed(Instant.parse("2026-10-24T23:30:00Z"), ZoneOffset.UTC)
        val dates = mutableListOf<LocalDate>()
        backgroundScope.launch { DashboardViewModel.currentDate(clock) { zone }.collect { dates += it } }
        advanceTimeBy(1)
        zone = ZoneId.of("America/New_York")
        advanceTimeBy(60_000)
        assertEquals(listOf(LocalDate.of(2026, 10, 25), LocalDate.of(2026, 10, 24)), dates)
    }
}

/** In-memory repository; every log is dated with [today]. */
private class FakeHabitLogRepository(private val today: () -> LocalDate) : HabitLogRepository {
    private val logs = MutableStateFlow(emptyList<HabitLog>())
    private var nextId = 1L

    fun add(habit: Habit, date: LocalDate): HabitLog {
        val log = HabitLog(habit, Instant.EPOCH, date, id = nextId++)
        logs.value = logs.value + log
        return log
    }

    override fun observeLogs(habit: Habit): Flow<List<HabitLog>> =
        logs.map { all -> all.filter { it.habit == habit } }

    override suspend fun log(habit: Habit): HabitLog = add(habit, today())

    override suspend fun delete(id: Long): Boolean {
        val before = logs.value.size
        logs.value = logs.value.filterNot { it.id == id }
        return logs.value.size < before
    }
}
