package com.example.lunasin.Frontend.UI.Statistic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Statistic.ChartEntry
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticData
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticViewModel
import com.example.lunasin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    navController: NavController? = null,
    // DIUBAH: ViewModel sekarang diterima sebagai parameter
    statisticViewModel: StatisticViewModel
) {
    // Pembuatan ViewModel dipindahkan ke NavGraph.kt

    val statisticData by statisticViewModel.statisticData.observeAsState(
        initial = StatisticData(isLoading = true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Statistik") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (statisticData.errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${statisticData.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { statisticViewModel.loadStatistics() }) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                StatisticContent(statisticData = statisticData)
            }
        }
    }
}

@Composable
private fun StatisticContent(statisticData: StatisticData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statisticData.dateRangeText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CustomLineChart(
            incomeEntries = statisticData.incomeEntries,
            expenseEntries = statisticData.expenseEntries,
            datesForXAxis = statisticData.datesForXAxis,
            incomeColor = MaterialTheme.colorScheme.tertiary,
            expenseColor = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                title = "Uang Masuk",
                amount = statisticData.totalMonthlyIncome,
                icon = R.drawable.ic_arrow_down_green,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Uang Keluar",
                amount = statisticData.totalExpense,
                icon = R.drawable.ic_arrow_up_red,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Detail Pengeluaran",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (statisticData.expenseByCategory.isEmpty()) {
            Text("Tidak ada data pengeluaran.", style = MaterialTheme.typography.bodyMedium)
        } else {
            statisticData.expenseByCategory.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                CategorySummaryItem(category = category, amount = amount)
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, amount: Double, icon: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = title, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelLarge, color = color)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Rp${String.format("%,.0f", amount)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun CustomLineChart(
    incomeEntries: List<ChartEntry>,
    expenseEntries: List<ChartEntry>,
    datesForXAxis: List<String>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier
) {
    if (datesForXAxis.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Butuh minimal 2 data untuk menampilkan chart.")
        }
        return
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val allEntries = incomeEntries + expenseEntries
        if (allEntries.isEmpty()) return@Canvas

        val maxY = (allEntries.maxOfOrNull { it.y } ?: 0f) * 1.1f
        if (maxY == 0f) return@Canvas

        val xStep = width / (datesForXAxis.size - 1).toFloat()

        fun drawDataPath(entries: List<ChartEntry>, color: Color) {
            if (entries.isEmpty()) return
            val path = Path()
            path.moveTo(entries.first().x * xStep, height - (entries.first().y / maxY) * height)
            entries.forEach { entry ->
                path.lineTo(entry.x * xStep, height - (entry.y / maxY) * height)
            }
            drawPath(path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }

        drawDataPath(incomeEntries, incomeColor)
        drawDataPath(expenseEntries, expenseColor)
    }
}

@Composable
fun CategorySummaryItem(category: String, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconResId = when (category) {
                "Gaji" -> R.drawable.ic_salary
                else -> R.drawable.ic_default_category
            }
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = category,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Rp${String.format("%,.0f", amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}