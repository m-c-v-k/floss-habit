package com.mcvk.flosshabit.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import com.mcvk.flosshabit.core.History
import com.mcvk.flosshabit.core.HistoryRange
import com.mcvk.flosshabit.core.StreakCalculator
import com.mcvk.flosshabit.data.HabitLogRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class DashboardViewModel(
    private val repository: HabitLogRepository,
    today: Flow<LocalDate> = currentDate(),
) : ViewModel() {

    private val undoable = MutableStateFlow<HabitLog?>(null)
    private val historyRange = MutableStateFlow(HistoryRange.WEEK)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeLogs(Habit.FLOSS),
        repository.observeLogs(Habit.MOUTHWASH),
        today,
        undoable,
        historyRange,
    ) { floss, mouthwash, date, lastLog, range ->
        // Both heatmaps cover the same days, starting at the earliest log of either habit.
        val firstLog = (floss + mouthwash).minOfOrNull { it.localDate }
        val period = History.period(range, date, firstLog)
        DashboardUiState(
            isLoading = false,
            cards = listOf(card(Habit.FLOSS, floss, date), card(Habit.MOUTHWASH, mouthwash, date)),
            undoable = lastLog,
            historyRange = range,
            history = listOf(history(Habit.FLOSS, floss, period, date), history(Habit.MOUTHWASH, mouthwash, period, date)),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onHistoryRangeSelected(range: HistoryRange) {
        historyRange.value = range
    }

    fun onDone(habit: Habit) {
        viewModelScope.launch { undoable.value = repository.log(habit) }
    }

    fun onUndo() {
        val log = undoable.value ?: return
        undoable.value = null
        log.id?.let { id -> viewModelScope.launch { repository.delete(id) } }
    }

    fun onUndoDismissed() {
        undoable.value = null
    }

    private fun history(habit: Habit, logs: List<HabitLog>, period: ClosedRange<LocalDate>, today: LocalDate): HabitHistoryState {
        val days = History.dailyCounts(logs, habit, period)
        return HabitHistoryState(
            habit = habit,
            totalDays = History.totalDays(logs, habit, today),
            days = days,
            weeks = History.weeks(days),
        )
    }

    private fun card(habit: Habit, logs: List<HabitLog>, today: LocalDate) = HabitCardState(
        habit = habit,
        logsToday = logs.count { it.localDate == today },
        streak = StreakCalculator.calculate(logs, habit, today),
    )

    companion object {
        /**
         * Today's date, checked every minute so the dashboard rolls over at midnight
         * and follows time zone changes without a restart.
         */
        fun currentDate(
            clock: Clock = Clock.systemUTC(),
            zone: () -> ZoneId = ZoneId::systemDefault,
        ): Flow<LocalDate> = flow {
            while (true) {
                emit(clock.instant().atZone(zone()).toLocalDate())
                delay(60_000)
            }
        }.distinctUntilChanged()
    }
}
