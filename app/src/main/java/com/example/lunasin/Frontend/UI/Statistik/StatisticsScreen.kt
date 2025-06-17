package com.example.lunasin.Frontend.UI.Statistik

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.Profile
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StatisticsScreen(userId: String) {
    var profile by remember { mutableStateOf(Profile()) }
    var debtData by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var totalDebt by remember { mutableStateOf(0.0) }
    var totalIncome by remember { mutableStateOf(0.0) } // Ambil dari monthlyIncome
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()

    // Fungsi untuk mengambil data hutang
    fun fetchDebtData() {
        val query1 = db.collection("hutang")
            .whereEqualTo("statusBayar", "BELUM_LUNAS")
            .whereEqualTo("userId", userId)

        val query2 = db.collection("hutang")
            .whereEqualTo("statusBayar", "BELUM_LUNAS")
            .whereEqualTo("id_penerima", userId)

        query1.get().addOnSuccessListener { documents1 ->
            query2.get().addOnSuccessListener { documents2 ->
                val combinedDocuments = documents1 + documents2
                val debtList = combinedDocuments.mapNotNull { Hutang.fromMap(it.data) }

                totalDebt = debtList.sumOf { it.nominalpinjaman }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                debtData = debtList.groupBy { it.tanggalPinjam }
                    .mapNotNull { entry ->
                        try {
                            val date = entry.key
                            val total = entry.value.sumOf { it.nominalpinjaman }
                            date to total
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error processing debt entry: ${e.message}")
                            null
                        }
                    }
                    .sortedBy { dateFormat.parse(it.first)?.time ?: 0L }

                Log.d("Firebase", "Hutang data retrieved: ${debtList.size} items")
                isLoading = false // Selesai setelah hutang dimuat
            }.addOnFailureListener { exception ->
                errorMessage = "Gagal memuat hutang (query2): ${exception.message}"
                Log.w("Firebase", "Error getting hutang (query2) for userId: $userId", exception)
                isLoading = false
            }
        }.addOnFailureListener { exception ->
            errorMessage = "Gagal memuat hutang (query1): ${exception.message}"
            Log.w("Firebase", "Error getting hutang (query1) for userId: $userId", exception)
            isLoading = false
        }
    }

    // Ambil data profil
    db.collection("profile")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                profile = Profile.fromMap(document.data ?: emptyMap())
                totalIncome = document.getDouble("monthlyIncome") ?: 0.0 // Ambil monthlyIncome dari profil
                Log.d("Firebase", "Profile retrieved for userId: $userId, monthlyIncome: $totalIncome")
                fetchDebtData() // Mulai dengan mengambil data hutang
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

    // Tampilkan data dan chart
    Column(modifier = Modifier.padding(16.dp)) {
        if (isLoading) {
            Text("Memuat data...", fontSize = 16.sp)
        } else if (errorMessage != null) {
            Text(errorMessage ?: "Terjadi kesalahan tidak diketahui", color = Color.Red, fontSize = 16.sp)
        } else {
            // Card untuk Total Hutang
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Hutang Anda", fontSize = 18.sp, color = Color.White)
                    Text("Rp${"%.0f".format(totalDebt)}", fontSize = 24.sp, color = Color.White)
                    Text("Perbarui informasi secara berkala", fontSize = 12.sp, color = Color.White)
                }
            }

            // Card untuk Total Pendapatan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Pendapatan Anda", fontSize = 18.sp, color = Color.White)
                    Text("Rp${"%.0f".format(totalIncome)}", fontSize = 24.sp, color = Color.White)
                    Text("Perbarui informasi secara berkala", fontSize = 12.sp, color = Color.White)
                }
            }

            // Chart untuk Pengeluaran dan Pendapatan
            if (debtData.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .weight(1f)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val paintDebt = android.graphics.Paint().apply {
                        color = android.graphics.Color.RED // Warna untuk hutang
                        strokeWidth = 2f
                    }
                    val paintIncome = android.graphics.Paint().apply {
                        color = android.graphics.Color.GREEN // Warna untuk pendapatan
                        strokeWidth = 2f
                    }

                    // Data untuk chart
                    val allDates = debtData.map { it.first } + listOf("Monthly") // Tambahkan "Monthly" untuk pendapatan
                    val debtValues = debtData.map { it.second } + listOf(0.0) // Pad dengan 0 untuk align dengan tanggal
                    val incomeValues = List(debtData.size) { 0.0 } + listOf(totalIncome) // Pendapatan hanya untuk "Monthly"

                    val maxValue = (debtValues + incomeValues).maxOrNull() ?: 1.0
                    val stepX = canvasWidth / (allDates.size - 1).coerceAtLeast(1)
                    val stepY = canvasHeight / maxValue.coerceAtLeast(1.0)

                    // Titik untuk hutang
                    val debtPoints = allDates.indices.map { i ->
                        Offset(i * stepX, canvasHeight - (debtValues[i] * stepY).toFloat())
                    }
                    val debtCoordinates = debtPoints.flatMap { listOf(it.x, it.y) }.toFloatArray()

                    // Titik untuk pendapatan
                    val incomePoints = allDates.indices.map { i ->
                        Offset(i * stepX, canvasHeight - (incomeValues[i] * stepY).toFloat())
                    }
                    val incomeCoordinates = incomePoints.flatMap { listOf(it.x, it.y) }.toFloatArray()

                    // Gambar garis untuk hutang
                    drawContext.canvas.nativeCanvas.apply {
                        drawLines(debtCoordinates, paintDebt)
                    }

                    // Gambar garis untuk pendapatan
                    drawContext.canvas.nativeCanvas.apply {
                        drawLines(incomeCoordinates, paintIncome)
                    }
                }
            } else {
                Text("Tidak ada data hutang untuk ditampilkan.", fontSize = 14.sp)
            }
        }
    }
}