package com.mcvk.flosshabit.dashboard

import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import com.mcvk.flosshabit.core.Streak

data class HabitCardState(
    val habit: Habit,
    /** Logs today; can be more than one. The streak counts the day once. */
    val logsToday: Int,
    val streak: Streak,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val cards: List<HabitCardState> = emptyList(),
    /** The log just made, offered for undo until the message is dismissed. */
    val undoable: HabitLog? = null,
)
