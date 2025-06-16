package com.example.lunasin.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.ZoneId // DIUBAH: Import baru yang diperlukan
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date // DIUBAH: Import baru yang diperlukan
import java.util.Locale


// DIUBAH: Signature fungsi sekarang menerima List<Date?>
@Composable
fun CalendarView(jatuhTempoDates: List<Date?>) {
    // DIUBAH: Logika untuk mengonversi List<Date?> menjadi Set<LocalDate>
    val jatuhTempoLocalDates = remember(jatuhTempoDates) {
        jatuhTempoDates.mapNotNull { date ->
            // Konversi java.util.Date ke java.time.LocalDate
            date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        }.toSet()
    }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        CalendarHeader(
            currentMonth = state.firstVisibleMonth.yearMonth,
            goToPreviousMonth = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1))
                }
            },
            goToNextMonth = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1))
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DaysOfWeekHeader()
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                // DIUBAH: Pengecekan menggunakan Set<LocalDate> yang baru
                Day(day, isJatuhTempo = jatuhTempoLocalDates.contains(day.date))
            }
        )
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    goToPreviousMonth: () -> Unit,
    goToNextMonth: () -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID"))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = goToPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya")
        }
        Text(
            text = currentMonth.format(monthFormatter).replaceFirstChar { it.titlecase(Locale.getDefault()) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = goToNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan Berikutnya")
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val daysOfWeek = DayOfWeek.values()
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("in", "ID")),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Day(day: CalendarDay, isJatuhTempo: Boolean) {
    Box(
        modifier = Modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (day.position == DayPosition.MonthDate) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isJatuhTempo) MaterialTheme.colorScheme.errorContainer else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (isJatuhTempo) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}