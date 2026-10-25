package com.mcvk.flosshabit.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mcvk.flosshabit.R
import com.mcvk.flosshabit.core.DayCount
import com.mcvk.flosshabit.core.HistoryRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Heatmaps of logs per day for both habits, with a range picker. */
@Composable
fun HistorySection(
    range: HistoryRange,
    history: List<HabitHistoryState>,
    onRangeSelected: (HistoryRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryRange.entries.forEach { option ->
                FilterChip(
                    selected = option == range,
                    onClick = { onRangeSelected(option) },
                    label = { Text(stringResource(option.labelRes)) },
                )
            }
        }
        history.forEach { habit ->
            HabitHeatmap(habit, range)
        }
        Legend()
    }
}

@Composable
private fun HabitHeatmap(state: HabitHistoryState, range: HistoryRange) {
    val locale = LocalConfiguration.current.locales[0]
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(state.habit.nameRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = pluralStringResource(R.plurals.total_days, state.totalDays, state.totalDays),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when (range) {
            HistoryRange.WEEK -> WeekRow(state.days, locale)
            HistoryRange.MONTH -> {
                state.days.firstOrNull()?.let { MonthLabel(it.date, locale) }
                WeekGrid(state.weeks, locale)
            }
            HistoryRange.ALL_TIME -> WeekGrid(state.weeks, locale)
        }
    }
}

/** The last 7 days in one row, each labelled with its weekday. */
@Composable
private fun WeekRow(days: List<DayCount>, locale: Locale) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        days.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                DayCell(day, locale)
                WeekdayLabel(day.date.dayOfWeek, locale)
            }
        }
    }
}

/** Calendar weeks as rows, Monday first, under a weekday header. */
@Composable
private fun WeekGrid(weeks: List<List<DayCount?>>, locale: Locale) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DayOfWeek.entries.forEach { WeekdayLabel(it, locale, Modifier.weight(1f)) }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    Box(Modifier.weight(1f)) {
                        if (day != null) DayCell(day, locale)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: DayCount, locale: Locale) {
    val date = day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    val description = pluralStringResource(R.plurals.day_logs, day.count, date, day.count)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(heatColor(day.count), RoundedCornerShape(4.dp))
            .semantics { contentDescription = description },
    )
}

@Composable
private fun WeekdayLabel(day: DayOfWeek, locale: Locale, modifier: Modifier = Modifier) {
    Text(
        text = day.getDisplayName(TextStyle.NARROW, locale),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun MonthLabel(date: LocalDate, locale: Locale) {
    val month = date.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    Text(
        text = month.replaceFirstChar { it.titlecase(locale) },
        style = MaterialTheme.typography.labelLarge,
    )
}

/** Fewer ▢▢▢▢ More: what the shades mean. */
@Composable
private fun Legend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val labelStyle = MaterialTheme.typography.labelSmall
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        Text(stringResource(R.string.history_fewer), style = labelStyle, color = labelColor)
        (0..3).forEach { count ->
            Box(Modifier.size(12.dp).background(heatColor(count), RoundedCornerShape(2.dp)))
        }
        Text(stringResource(R.string.history_more), style = labelStyle, color = labelColor)
    }
}

/** No logs, 1, 2, and 3 or more get increasingly strong shades of the primary colour. */
@Composable
private fun heatColor(count: Int): Color {
    val primary = MaterialTheme.colorScheme.primary
    return when {
        count <= 0 -> MaterialTheme.colorScheme.surfaceVariant
        count == 1 -> primary.copy(alpha = 0.35f)
        count == 2 -> primary.copy(alpha = 0.65f)
        else -> primary
    }
}

private val HistoryRange.labelRes: Int
    get() = when (this) {
        HistoryRange.WEEK -> R.string.history_week
        HistoryRange.MONTH -> R.string.history_month
        HistoryRange.ALL_TIME -> R.string.history_all_time
    }
