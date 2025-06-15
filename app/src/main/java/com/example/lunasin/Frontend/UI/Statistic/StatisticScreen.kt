package com.example.lunasin.Frontend.UI.Statistic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lunasin.Backend.Data.management_data.HutangRepository
import com.example.lunasin.Backend.Data.profile_data.ProfileRepository
import com.example.lunasin.Frontend.viewmodel.Statistic.ChartEntry
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticData
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticViewModel
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticViewModelFactory
import com.example.lunasin.R
import com.google.firebase.firestore.FirebaseFirestore

// HAPUS DEFINISI DATA CLASS ChartEntry DARI SINI!
// data class ChartEntry(
//     val x: Float, // Posisi di sumbu X (misalnya, indeks hari dalam sebulan)
//     val y: Float  // Nilai di sumbu Y (misalnya, jumlah uang)
// )

// =====================================================================
// Statistik Screen Utama
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    navController: NavController? = null,
    statisticViewModel: StatisticViewModel = viewModel(
        factory = StatisticViewModelFactory(
            // PENTING: Lewatkan LocalContext.current ke ProfileRepository
            profileRepository = ProfileRepository(LocalContext.current),
            hutangRepository = HutangRepository(FirebaseFirestore.getInstance())
        )
    )
) {
    val statisticData by statisticViewModel.statisticData.observeAsState(
        initial = StatisticData(
            totalMonthlyIncome = 0.0,
            totalExpense = 0.0,
            incomeEntries = emptyList(), // Ini akan menggunakan ChartEntry dari import di atas
            expenseEntries = emptyList(), // Ini akan menggunakan ChartEntry dari import di atas
            datesForXAxis = emptyList(),
            expenseByCategory = emptyMap(),
            dateRangeText = "Memuat data...",
            isLoading = true,
            errorMessage = null
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DANA Statement") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (statisticData.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally))
                Text("Memuat data...", modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally))
            } else if (statisticData.errorMessage != null) {
                Text(
                    text = "Error: ${statisticData.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
                Button(
                    onClick = { statisticViewModel.loadStatistics() },
                    modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
                ) {
                    Text("Coba Lagi")
                }
            } else {
                // Rentang Tanggal
                Text(
                    text = statisticData.dateRangeText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                // Custom Line Chart Composable
                CustomLineChart(
                    incomeEntries = statisticData.incomeEntries,
                    expenseEntries = statisticData.expenseEntries,
                    datesForXAxis = statisticData.datesForXAxis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )

                // Ringkasan Pemasukan/Pengeluaran
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_down_green),
                                    contentDescription = "Uang Masuk",
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Uang Masuk",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Text(
                                text = "Rp${String.format("%,.0f", statisticData.totalMonthlyIncome)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_up_red),
                                    contentDescription = "Uang Keluar",
                                    tint = Color(0xFFFF5722)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Uang Keluar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF5722)
                                )
                            }
                            Text(
                                text = "Rp${String.format("%,.0f", statisticData.totalExpense)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kategori Pengeluaran
                Text(
                    text = "Detail Pengeluaran Berdasarkan Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column {
                    statisticData.expenseByCategory.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                        CategorySummaryItem(category = category, amount = amount)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Detail dapat dilihat di Riwayat Transaksi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { /* TODO: Implement Export Statement logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("EXPORT STATEMENT", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

// =========================================================
// Custom Line Chart Composable (Implementasi dasar dengan Canvas)
// =========================================================
@Composable
fun CustomLineChart(
    incomeEntries: List<ChartEntry>,
    expenseEntries: List<ChartEntry>,
    datesForXAxis: List<String>,
    modifier: Modifier = Modifier
) {
    val incomeColor = Color(0xFF4CAF50)
    val expenseColor = Color(0xFFFF5722)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val allEntries = incomeEntries + expenseEntries
        val maxY = if (allEntries.isNotEmpty()) {
            (allEntries.maxOfOrNull { it.y } ?: 0f) * 1.1f
        } else 1f

        if (maxY == 0f) {
            return@Canvas
        }

        val xStep = if (datesForXAxis.size > 1) width / (datesForXAxis.size - 1).toFloat() else width

        fun drawDataPath(entries: List<ChartEntry>, color: androidx.compose.ui.graphics.Color) {
            if (entries.isEmpty()) return

            val path = Path()
            val firstX = entries.first().x * xStep
            val firstY = height - (entries.first().y / maxY) * height
            path.moveTo(firstX, firstY)

            entries.forEach { entry ->
                val x = entry.x * xStep
                val y = height - (entry.y / maxY) * height
                path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }

        drawDataPath(incomeEntries, incomeColor)
        drawDataPath(expenseEntries, expenseColor)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val labelYOffset = 40f

        datesForXAxis.forEachIndexed { index, dateLabel ->
            val x = index * xStep
            drawContext.canvas.nativeCanvas.drawText(
                dateLabel,
                x,
                height + labelYOffset,
                textPaint
            )
        }
    }
}


// =========================================================
// Category Summary Item Composable
// =========================================================
@Composable
fun CategorySummaryItem(category: String, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val iconResId = when (category) {
                "Gaji" -> R.drawable.ic_salary
//                "Belanja" -> R.drawable.ic_shopping // AKTIFKAN KEMBALI KATEGORI INI
//                "Transportasi" -> R.drawable.ic_transport // AKTIFKAN KEMBALI KATEGORI INI
//                "Makanan" -> R.drawable.ic_food // AKTIFKAN KEMBALI KATEGORI INI
//                "Tagihan" -> R.drawable.ic_bills // AKTIFKAN KEMBALI KATEGORI INI
//                "Edukasi" -> R.drawable.ic_education // AKTIFKAN KEMBALI KATEGORI INI
//                "alganana" -> R.drawable.ic_person // AKTIFKAN KEMBALI KATEGORI INI
                else -> R.drawable.ic_default_category
            }
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category,
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = category,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rp${String.format("%,.0f", amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Untuk Preview di Android Studio (opsional)
// @Preview(showBackground = true)
// @Composable
// fun StatisticScreenPreview() {
//     LunasinTheme { // Ganti dengan nama tema Compose Anda
//         StatisticScreen()
//     }
// }