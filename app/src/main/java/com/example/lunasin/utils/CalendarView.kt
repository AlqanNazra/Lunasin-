package com.example.lunasin.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarView(jatuhTempoList: List<String>) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
    val daysInMonth = currentMonth.lengthOfMonth()
    val dueDates = jatuhTempoList.mapNotNull { dateStr ->
        try {
            val parts = dateStr.split("/")
            LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
        } catch (e: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Text(text = "<", fontSize = 20.sp, color = Color.Green)
                }
                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.h6
                )
            }
            Text(
                text = currentMonth.year.toString(),
                style = MaterialTheme.typography.h6,
                color = Color.Green
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text(text = ">", fontSize = 20.sp, color = Color.Green)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.values().forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Kalender Manual
        val offset = firstDayOfMonth.value % 7 // Sesuaikan untuk mulai dari Senin
        var dayCounter = 1
        Column {
            for (week in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 0 until 7) {
                        val adjustedDay = day - offset + 1 + (week * 7)
                        if (adjustedDay in 1..daysInMonth) {
                            val currentDate = currentMonth.atDay(adjustedDay)
                            val isDueDate = dueDates.any { it == currentDate }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDueDate) Color.Red else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = adjustedDay.toString(),
                                    color = if (isDueDate) Color.White else Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).size(40.dp))
                        }
                    }
                }
            }
        }
    }
}