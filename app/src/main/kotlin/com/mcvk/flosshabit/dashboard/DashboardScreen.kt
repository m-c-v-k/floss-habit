package com.mcvk.flosshabit.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mcvk.flosshabit.R
import com.mcvk.flosshabit.core.Habit
import com.mcvk.flosshabit.core.HabitLog
import com.mcvk.flosshabit.core.Streak
import com.mcvk.flosshabit.ui.theme.FlossHabitTheme

@Composable
fun DashboardRoute(viewModel: DashboardViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        onDone = viewModel::onDone,
        onUndo = viewModel::onUndo,
        onUndoDismissed = viewModel::onUndoDismissed,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onDone: (Habit) -> Unit,
    onUndo: () -> Unit,
    onUndoDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UndoSnackbar(state.undoable, snackbarHostState, onUndo, onUndoDismissed)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            state.cards.forEach { card ->
                HabitCard(card, onDone = { onDone(card.habit) })
            }
        }
    }
}

@Composable
private fun UndoSnackbar(
    undoable: HabitLog?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onUndoDismissed: () -> Unit,
) {
    if (undoable == null) return
    val message = stringResource(R.string.logged_message, stringResource(undoable.habit.nameRes))
    val undoLabel = stringResource(R.string.undo)
    LaunchedEffect(undoable) {
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) onUndo() else onUndoDismissed()
    }
}

@Composable
private fun HabitCard(card: HabitCardState, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(card.habit.nameRes),
                style = MaterialTheme.typography.titleLarge,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (card.logsToday > 0) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(pluralStringResource(R.plurals.done_today, card.logsToday, card.logsToday))
                } else {
                    Text(stringResource(R.string.not_done_today))
                }
            }
            Text(
                text = stringResource(
                    R.string.streaks,
                    pluralStringResource(R.plurals.days, card.streak.current, card.streak.current),
                    pluralStringResource(R.plurals.days, card.streak.longest, card.streak.longest),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (card.logsToday == 0) {
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.done))
                }
            } else {
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.log_again))
                }
            }
        }
    }
}

@get:StringRes
private val Habit.nameRes: Int
    get() = when (this) {
        Habit.FLOSS -> R.string.habit_floss
        Habit.MOUTHWASH -> R.string.habit_mouthwash
    }

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    FlossHabitTheme {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                cards = listOf(
                    HabitCardState(Habit.FLOSS, logsToday = 2, streak = Streak(current = 4, longest = 9, doneToday = true)),
                    HabitCardState(Habit.MOUTHWASH, logsToday = 0, streak = Streak(current = 1, longest = 3, doneToday = false)),
                ),
            ),
            onDone = {},
            onUndo = {},
            onUndoDismissed = {},
        )
    }
}
