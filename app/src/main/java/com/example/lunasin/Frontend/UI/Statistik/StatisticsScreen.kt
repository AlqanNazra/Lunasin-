package com.example.lunasin.Frontend.UI.Statistik

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.example.lunasin.Frontend.UI.Navigation.BottomNavigationBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.Profile
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun StatisticsScreen(
    navController: NavController,
    userId: String,
    hutangViewModel: HutangViewModel,
    piutangViewModel: PiutangViewModel
) {

    var profile by remember { mutableStateOf(Profile()) }
    var debtData by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var receivableData by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var totalDebt by remember { mutableStateOf(0.0) }
    var totalReceivable by remember { mutableStateOf(0.0) }
    var totalIncome by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var availableYears by remember { mutableStateOf(listOf<Int>()) }

    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    // Fetch profile data
    LaunchedEffect(userId) {
        db.collection("profile")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    profile = Profile.fromMap(document.data ?: emptyMap())
                    totalIncome = document.getDouble("monthlyIncome") ?: 0.0
                    Log.d("Firebase", "Profile retrieved for userId: $userId, monthlyIncome: $totalIncome")
                    hutangViewModel.ambilHutangSaya(userId)
                    piutangViewModel.ambilPiutangSaya(userId)
                } else {
                    errorMessage = "Profil tidak ditemukan untuk userId: $userId"
                    Log.w("Firebase", "Profile not found for userId: $userId")
                    isLoading = false
                }
            }
            .addOnFailureListener { exception ->
                errorMessage = "Gagal memuat profil: ${exception.message}"
                Log.w("Firebase", "Error getting profile for userId: $userId", exception)
                isLoading = false
            }
    }

    // Collect hutang and piutang data and compute available years
    LaunchedEffect(userId) {
        hutangViewModel.hutangSayaList.collectLatest { hutangList ->
            piutangViewModel.piutangSayaList.collectLatest { piutangList ->
                // Process hutang (debts)
                totalDebt = hutangList.sumOf { it.totalHutang ?: 0.0 }
                debtData = hutangList
                    .filter { it.tanggalPinjam.isNotBlank() }
                    .mapNotNull { hutang ->
                        try {
                            val date = dateFormat.parse(hutang.tanggalPinjam)
                            val monthYear = monthYearFormat.format(date)
                            val month = monthFormat.format(date)
                            if (selectedYear in monthYear) month to (hutang.totalHutang ?: 0.0) else null
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error parsing date for hutang: ${e.message}")
                            null
                        }
                    }
                    .groupBy { it.first }
                    .map { entry -> Pair(entry.key, entry.value.sumOf { it.second }) }
                    .sortedBy { monthFormat.parse(it.first)?.time ?: 0L }

                // Process piutang (receivables)
                totalReceivable = piutangList.sumOf { it.totalHutang ?: 0.0 }
                receivableData = piutangList
                    .filter { it.tanggalPinjam.isNotBlank() }
                    .mapNotNull { piutang ->
                        try {
                            val date = dateFormat.parse(piutang.tanggalPinjam)
                            val monthYear = monthYearFormat.format(date)
                            val month = monthFormat.format(date)
                            if (selectedYear in monthYear) month to (piutang.totalHutang ?: 0.0) else null
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error parsing date for piutang: ${e.message}")
                            null
                        }
                    }
                    .groupBy { it.first }
                    .map { entry -> Pair(entry.key, entry.value.sumOf { it.second }) }
                    .sortedBy { monthFormat.parse(it.first)?.time ?: 0L }

                // Compute available years
                val allDates = (hutangList + piutangList)
                    .mapNotNull { it.tanggalPinjam }
                    .mapNotNull { dateFormat.parse(it)?.let { monthYearFormat.format(it) } }
                    .mapNotNull { it.split(" ").last().toIntOrNull() }
                    .distinct()
                    .sorted()
                availableYears = if (allDates.isEmpty()) listOf(Calendar.getInstance().get(Calendar.YEAR)) else allDates

                isLoading = false
                Log.d("Firebase", "Hutang: ${hutangList.size}, Piutang: ${piutangList.size}")
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statistik Keuangan",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "2025 6", // Current date: June 17, 2025, 11:06 PM WIB
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (errorMessage != null) {
                    Text(
                        errorMessage ?: "Terjadi kesalahan tidak diketahui",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    SwipableStatCards(totalDebt, totalReceivable)
                    DebtReceivableBarChart(debtData, receivableData, totalIncome, selectedYear, availableYears) { year ->
                        selectedYear = year
                    }
                }
            }
        }
    )
}

@Composable
fun SwipableStatCards(totalDebt: Double, totalReceivable: Double) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val titles = listOf("Total Hutang", "Total Piutang")
    val values = listOf(totalDebt, totalReceivable)
    val colors = listOf(Color(0xFF3F51B5), Color(0xFF009688))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        for (page in 0..1) {
            val offsetFactor = (page - pagerState.currentPage).toFloat()
            val animatedOffset by animateFloatAsState(
                targetValue = offsetFactor * 40f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "offset"
            )
            val animatedScale by animateFloatAsState(
                targetValue = 1f - (offsetFactor.absoluteValue * 0.05f),
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "scale"
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f - (offsetFactor.absoluteValue * 0.2f),
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        translationY = animatedOffset
                        scaleX = animatedScale
                        scaleY = animatedScale
                        alpha = animatedAlpha
                    }
                    .zIndex(1f - offsetFactor.absoluteValue)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color = colors[page])
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = titles[page],
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(values[page])}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        VerticalPager(
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier
                .matchParentSize()
                .zIndex(2f)
                .background(Color.Transparent)
        ) {}
    }
}

@Composable
fun DebtReceivableBarChart(
    debtData: List<Pair<String, Double>>,
    receivableData: List<Pair<String, Double>>,
    totalIncome: Double,
    selectedYear: String,
    availableYears: List<Int>,
    onYearChange: (String) -> Unit
) {
    var showYearDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Hutang & Piutang Per Bulan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        if (debtData.isEmpty() && receivableData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada data untuk ditampilkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        } else {
            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF3F51B5))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hutang", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF009688))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Piutang", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFFF9800))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pendapatan", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Year Selection Button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = { showYearDropdown = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = selectedYear,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp
                    )
                }
                DropdownMenu(
                    expanded = showYearDropdown,
                    onDismissRequest = { showYearDropdown = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    availableYears.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString(), style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onYearChange(year.toString())
                                showYearDropdown = false
                            }
                        )
                    }
                }
            }

            // Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val barWidth = canvasWidth / 12f
                    val maxValue = (debtData.map { it.second } + receivableData.map { it.second } + listOf(totalIncome))
                        .maxOrNull() ?: 1.0
                    val stepY = canvasHeight / maxValue.coerceAtLeast(1.0)

                    val paintDebt = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#3F51B5")
                        style = android.graphics.Paint.Style.FILL
                    }
                    val paintReceivable = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#009688")
                        style = android.graphics.Paint.Style.FILL
                    }
                    val paintIncomeLine = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#FF9800")
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 4f
                    }
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val yLabelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 16f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }

                    // Draw y-axis labels (Rupiah)
                    val yStep = maxValue / 5
                    for (i in 0..5) {
                        val value = i * yStep
                        val y = canvasHeight - (value * stepY).toFloat()
                        drawContext.canvas.nativeCanvas.drawText(
                            "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(value)}",
                            50f,
                            y + 5f,
                            yLabelPaint
                        )
                    }

                    // Draw x-axis labels (Months)
                    months.forEachIndexed { index, month ->
                        val x = index * barWidth + barWidth / 2
                        drawContext.canvas.nativeCanvas.drawText(
                            month,
                            x,
                            canvasHeight + 20f,
                            textPaint
                        )
                    }

                    // Draw bars for debt and receivable
                    months.forEachIndexed { index, month ->
                        val debtAmount = debtData.find { it.first == month }?.second ?: 0.0
                        val receivableAmount = receivableData.find { it.first == month }?.second ?: 0.0
                        val x = index * barWidth

                        // Debt bar
                        val debtHeight = (debtAmount * stepY).toFloat()
                        drawContext.canvas.nativeCanvas.drawRect(
                            x,
                            canvasHeight - debtHeight,
                            x + barWidth * 0.45f,
                            canvasHeight,
                            paintDebt
                        )

                        // Receivable bar
                        val receivableHeight = (receivableAmount * stepY).toFloat()
                        drawContext.canvas.nativeCanvas.drawRect(
                            x + barWidth * 0.55f,
                            canvasHeight - receivableHeight,
                            x + barWidth,
                            canvasHeight,
                            paintReceivable
                        )
                    }

                    // Draw income reference line
                    if (totalIncome > 0) {
                        val incomeY = canvasHeight - (totalIncome * stepY).toFloat()
                        drawContext.canvas.nativeCanvas.drawLine(
                            0f, incomeY, canvasWidth, incomeY, paintIncomeLine
                        )
                    }

                    // Draw axes
                    drawContext.canvas.nativeCanvas.drawLine(0f, 0f, 0f, canvasHeight, textPaint)
                    drawContext.canvas.nativeCanvas.drawLine(0f, canvasHeight, canvasWidth, canvasHeight, textPaint)
                }
            }
        }
    }
}