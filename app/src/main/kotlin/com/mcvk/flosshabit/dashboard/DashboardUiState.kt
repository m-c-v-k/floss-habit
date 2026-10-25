package com.mcvk.flosshabit.dashboard

import com.mcvk.flosshabit.core.DayCount
import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import com.mcvk.flosshabit.core.HistoryRange
import com.mcvk.flosshabit.core.Streak

data class HabitCardState(
    val habit: Habit,
    /** Logs today; can be more than one. The streak counts the day once. */
    val logsToday: Int,
    val streak: Streak,
)

/** One habit's heatmap for the selected range. */
data class HabitHistoryState(
    val habit: Habit,
    /** Days with at least one log, all time. */
    val totalDays: Int,
    /** Every day in the range, oldest first. */
    val days: List<DayCount>,
    /** The same days as Monday-first calendar weeks, padded with nulls. */
    val weeks: List<List<DayCount?>>,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val cards: List<HabitCardState> = emptyList(),
    /** The log just made, offered for undo until the message is dismissed. */
    val undoable: HabitLog? = null,
    val historyRange: HistoryRange = HistoryRange.WEEK,
    val history: List<HabitHistoryState> = emptyList(),
)
