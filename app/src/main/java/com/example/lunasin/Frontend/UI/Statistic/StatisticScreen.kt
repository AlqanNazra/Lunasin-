package com.example.lunasin.Frontend.UI.Statistic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

// HAPUS DEFINISI DATA CLASS ChartEntry DARI SINI JIKA MASIH ADA
// data class ChartEntry(...)

// =====================================================================
// Statistik Screen Utama
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    navController: NavController? = null,
    statisticViewModel: StatisticViewModel = viewModel(
        factory = StatisticViewModelFactory(
            profileRepository = ProfileRepository(LocalContext.current), // PENTING: Melewatkan LocalContext.current
            hutangRepository = HutangRepository(FirebaseFirestore.getInstance())
        )
    )
) {
    val statisticData by statisticViewModel.statisticData.observeAsState(
        initial = StatisticData(
            totalMonthlyIncome = 0.0,
            totalExpense = 0.0,
            incomeEntries = emptyList(), // Ini akan menggunakan ChartEntry dari import
            expenseEntries = emptyList(), // Ini akan menggunakan ChartEntry dari import
            datesForXAxis = emptyList(),
            expenseByCategory = emptyMap(),
            dateRangeText = "Memuat data...",
            isLoading = true,
            errorMessage = null
        )
    )

    // Definisikan ColorScheme untuk tema Compose lokal Anda (Material Design 3)
    // Anda bisa menyesuaikan warna-warna ini agar sesuai dengan palet aplikasi Anda
    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF673AB7), // Contoh warna primary (deep purple)
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71), // Contoh warna secondary
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF7D5260),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFFFFFF), // Putih
        onBackground = Color(0xFF1C1B1F), // Hampir hitam
        surface = Color(0xFFFFFFFF), // Putih
        onSurface = Color(0xFF1C1B1F), // Hampir hitam
        surfaceVariant = Color(0xFFE7E0EC), // Abu-abu terang
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF7A757F),
        outlineVariant = Color(0xFFCAC4CF),
        scrim = Color(0x00000000),
        inverseSurface = Color(0xFF313033),
        inverseOnSurface = Color(0xFFF4EFF4),
        inversePrimary = Color(0xFFD0BCFF),

        surfaceBright = Color(0xFFFBFAFF),
        surfaceDim = Color(0xFFDED8E2),
        surfaceContainer = Color(0xFFEBE5ED),
        surfaceContainerHigh = Color(0xFFE7E0EC),
        surfaceContainerHighest = Color(0xFFE4DBE6),
        surfaceContainerLow = Color(0xFFF2EDF5),
        surfaceContainerLowest = Color(0xFFFFFFFF)
    )

    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF2B8B5),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4CF),
        outline = Color(0xFF948F99),
        outlineVariant = Color(0xFF49454F),
        scrim = Color(0x00000000),
        inverseSurface = Color(0xFFE6E1E5),
        inverseOnSurface = Color(0xFF313033),
        inversePrimary = Color(0xFF673AB7),

        surfaceBright = Color(0xFF3F3D42),
        surfaceDim = Color(0xFF1C1B1F),
        surfaceContainer = Color(0xFF232126),
        surfaceContainerHigh = Color(0xFF2E2C31),
        surfaceContainerHighest = Color(0xFF363439),
        surfaceContainerLow = Color(0xFF28262B),
        surfaceContainerLowest = Color(0xFF121014)
    )

    // Deteksi apakah sistem dalam mode gelap atau terang
    val colors = if (isSystemInDarkTheme()) darkColorScheme else lightColorScheme

    // --- PENTING: BUNGKUS SELURUH KONTEN DI DALAM MaterialTheme INI ---
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography, // Gunakan tipografi default Material3
        shapes = MaterialTheme.shapes // Gunakan shapes default Material3
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("DANA Statement") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Mengambil warna dari tema lokal
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
                                MaterialTheme.colorScheme.surfaceVariant, // Mengambil warna dari tema lokal
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
                                        tint = Color(0xFF4CAF50) // Warna hardcoded di sini
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
                                        tint = Color(0xFFFF5722) // Warna hardcoded di sini
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
    val incomeColor = Color(0xFF4CAF50) // Green
    val expenseColor = Color(0xFFFF5722) // Orange

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
//                "Belanja" -> R.drawable.ic_shopping
//                "Transportasi" -> R.drawable.ic_transport
//                "Makanan" -> R.drawable.ic_food
//                "Tagihan" -> R.drawable.ic_bills
//                "Edukasi" -> R.drawable.ic_education
//                "alganana" -> R.drawable.ic_person
                else -> R.drawable.ic_default_category
            }
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category,
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF555555) // Warna tint ikon, sesuaikan dengan colors.xml jika perlu
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