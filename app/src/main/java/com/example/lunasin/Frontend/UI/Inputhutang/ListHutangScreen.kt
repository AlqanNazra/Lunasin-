package com.example.lunasin.Frontend.UI.Inputhutang

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel

@Composable
fun ListHutangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    val hutangList by hutangViewModel.hutangList.collectAsState()

    LaunchedEffect(Unit) {
        hutangViewModel.ambilDataHutang()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Hutang") },
                backgroundColor = Color.White,
                elevation = 4.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("input_hutang_screen") // Navigasi ke InputHutangScreen
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            hutangList.forEach { hutang ->
                HutangItem(hutang, navController)
            }
        }
    }
}

@Composable
fun HutangItem(hutang: Hutang, navController: NavHostController) {
    val statusLunas = hutang.totalHutang == 0.0
    val statusColor = if (statusLunas) Color.Blue else Color.Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pemberi Hutang", fontWeight = FontWeight.Bold)
                Text(hutang.namapinjaman, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tanggal Tempo: ${hutang.tanggalPinjam}", fontSize = 14.sp)
            Text("Tanggal Dibayar: ${hutang.tanggalBayar}", fontSize = 14.sp)
            Text("Nominal: ${hutang.nominalpinjaman}", fontSize = 14.sp, color = Color.Blue)

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Button Navigasi ke Preview
            Button(
                onClick = {
                    Log.d("ListHutangScreen", "Button diklik, docId: ${hutang.docId}")
                    if (hutang.docId.isNotEmpty()) {
                        navController.navigate("preview_hutang/${hutang.docId}")
                        Log.d("ListHutangScreen", "Navigasi ke preview_hutang/${hutang.docId}")
                    } else {
                        Log.e("ListHutangScreen", "Error: docId kosong!")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lihat Detail")
            }
        }
    }
}
