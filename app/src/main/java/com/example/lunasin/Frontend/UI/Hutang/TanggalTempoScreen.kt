package com.example.lunasin.Frontend.UI.Hutang

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.utils.normalizeDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.lunasin.Backend.Model.Tempo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanggalTempoScreen(viewModel: HutangViewModel, navController: NavController, docId: String) {
    val hutangState by viewModel.hutangState.collectAsState()
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    var selectedDate by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(docId) {
        if (docId.isNotEmpty()) {
            viewModel.getHutangById(docId) { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.White
                ),
                actions = {}
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Tanggal Tempo & Pinjam",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hari Ini: $today",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        hutangState?.let { hutang ->
                            Text(
                                text = "Jenis Hutang: ${hutang.hutangType?.name ?: "Tidak Diketahui"}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tanggal Pinjam: ${normalizeDate(hutang.tanggalPinjam)}",
                                fontSize = 14.sp,
                                color = if (normalizeDate(hutang.tanggalPinjam) == today) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Status Bayar: ${hutang.statusBayar?.name ?: "BELUM_LUNAS"}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } ?: Text(
                            text = "Memuat data...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                hutangState?.let { hutang ->
                    val highlightDates = when (hutang.hutangType) {
                        HutangType.SERIUS -> {
                            val tempoDates = hutang.listTempo.map { normalizeDate(it.tanggalTempo) }
                            listOf(normalizeDate(hutang.tanggalPinjam)) + tempoDates
                        }
                        else -> {
                            listOf(normalizeDate(hutang.tanggalPinjam), normalizeDate(hutang.tanggalJatuhTempo))
                        }
                    }.filter { it != "Format Salah" }

                    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
                    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
                    val daysInMonth = currentMonth.lengthOfMonth()

                    val normalizedHighlightDates = highlightDates.map { normalizeDate(it) }
                    val normalizedToday = normalizeDate(today)

                    val dueDates = normalizedHighlightDates.mapNotNull { dateStr ->
                        try {
                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            LocalDate.parse(dateStr, formatter)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val todayDate = try {
                        LocalDate.parse(normalizedToday, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } catch (e: Exception) {
                        LocalDate.now()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                                Text(
                                    text = "<",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                Text(
                                    text = ">",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DayOfWeek.values().forEach { dayOfWeek ->
                                Text(
                                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val offset = (firstDayOfMonth.value + 6) % 7
                        Column {
                            for (week in 0 until 6) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (day in 0 until 7) {
                                        val adjustedDay = day - offset + 1 + (week * 7)
                                        if (adjustedDay in 1..daysInMonth) {
                                            val currentDate = currentMonth.atDay(adjustedDay)
                                            val isDueDate = dueDates.any { it == currentDate }
                                            val isToday = currentDate == todayDate
                                            val dateStr = currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isToday -> MaterialTheme.colorScheme.error
                                                            isDueDate -> MaterialTheme.colorScheme.primary
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .clickable(enabled = isDueDate) {
                                                        if (isDueDate) {
                                                            selectedDate = dateStr
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = adjustedDay.toString(),
                                                    color = when {
                                                        isToday -> MaterialTheme.colorScheme.onError
                                                        isDueDate -> MaterialTheme.colorScheme.onPrimary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
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
                } ?: Text(
                    text = "Memuat kalender...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }

            item {
                AnimatedVisibility(
                    visible = selectedDate != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedDate?.let { date ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                hutangState?.let { hutang ->
                                    val isTanggalPinjam = normalizeDate(hutang.tanggalPinjam) == date
                                    val isTanggalTempoSerius = hutang.hutangType == HutangType.SERIUS &&
                                            hutang.listTempo.any { normalizeDate(it.tanggalTempo) == date }
                                    val isTanggalTempoLain =
                                        (hutang.hutangType == HutangType.TEMAN || hutang.hutangType == HutangType.PERHITUNGAN) &&
                                                normalizeDate(hutang.tanggalJatuhTempo) == date

                                    if (isTanggalPinjam) {
                                        Text(
                                            text = "Tanggal Pinjam: $date",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isTanggalTempoSerius || isTanggalTempoLain) {
                                        Text(
                                            text = "Tanggal Jatuh Tempo: $date",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val daysLeft = calculateDaysLeft(date)
                                        Text(
                                            text = "Sisa Hari: ${if (daysLeft >= 0) "$daysLeft hari" else "Terlewat ${-daysLeft} hari"}",
                                            color = if (daysLeft < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                var isDetailsVisible by remember { mutableStateOf(false) }

                Column {
                    Button(
                        onClick = { isDetailsVisible = !isDetailsVisible },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isDetailsVisible) "See Less" else "See Details",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    AnimatedVisibility(
                        visible = isDetailsVisible && hutangState != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            when (hutangState?.hutangType) {
                                HutangType.SERIUS -> {
                                    hutangState?.listTempo?.forEach { tempo ->
                                        TempoItem(
                                            tempo = tempo,
                                            normalizedDate = normalizeDate(tempo.tanggalTempo),
                                            today = today
                                        )
                                    }
                                }
                                HutangType.TEMAN, HutangType.PERHITUNGAN -> {
                                    hutangState?.let { hutang ->
                                        TempoItem(
                                            tempo = Tempo(1, hutang.tanggalJatuhTempo),
                                            normalizedDate = normalizeDate(hutang.tanggalJatuhTempo),
                                            today = today
                                        )
                                    }
                                }
                                else -> {
                                    Text("Jenis hutang tidak dikenali", modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TempoItem(tempo: Tempo, normalizedDate: String, today: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Handle click if needed */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (normalizedDate == today) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pembayaran ${tempo.angsuranKe}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tanggal Tempo: $normalizedDate",
                    color = if (normalizedDate == today) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                val daysLeft = calculateDaysLeft(normalizedDate)
                Text(
                    text = "Sisa Hari: ${if (daysLeft >= 0) daysLeft else "Terlewat ${-daysLeft} hari"}",
                    color = if (daysLeft < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

fun calculateDaysLeft(tanggalTempo: String): Long {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val tempoDate = LocalDate.parse(tanggalTempo, formatter)
        val today = LocalDate.now()
        ChronoUnit.DAYS.between(today, tempoDate) // Hitung selisih hari secara akurat
    } catch (e: Exception) {
        Log.e("TanggalError", "Gagal hitung sisa hari: $tanggalTempo", e)
        0L
    }
}