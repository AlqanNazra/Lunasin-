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
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun CalendarView(jatuhTempoList: List<String>) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }

    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar.add(Calendar.YEAR, -1) // Tahun sebelumnya
            }) {
                Text(text = "<<", fontSize = MaterialTheme.typography.h6.fontSize)
            }

            IconButton(onClick = {
                calendar.add(Calendar.MONTH, -1) // Bulan sebelumnya
            }) {
                Text(text = "<", fontSize = MaterialTheme.typography.h6.fontSize)
            }

            Text(text = "Kalender ${currentMonth + 1}/$currentYear", style = MaterialTheme.typography.h6)

            IconButton(onClick = {
                calendar.add(Calendar.MONTH, 1) // Bulan berikutnya
            }) {
                Text(text = ">", fontSize = MaterialTheme.typography.h6.fontSize)
            }

            IconButton(onClick = {
                calendar.add(Calendar.YEAR, 1) // Tahun berikutnya
            }) {
                Text(text = ">>", fontSize = MaterialTheme.typography.h6.fontSize)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Kalender
        Column {
            for (week in 0 until 5) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (day in 1..7) {
                        val date = (week * 7 + day)
                        if (date <= daysInMonth) {
                            val isJatuhTempo = jatuhTempoList.contains("$date/${currentMonth + 1}/$currentYear")
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isJatuhTempo) Color.Red else Color.Transparent),
//                                    .clickable { /* Bisa tambahkan aksi kalau perlu */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = date.toString(), color = if (isJatuhTempo) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
