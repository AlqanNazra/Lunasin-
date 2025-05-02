package com.example.lunasin.Frontend.UI.Inputhutang

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.utils.CalendarView

@Composable
fun TanggalBayarScreen(viewModel: HutangViewModel, navController: NavController, docId: String) {
    val hutangState by viewModel.hutangState.collectAsState()

    LaunchedEffect(docId) {
        viewModel.getHutangById(docId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Tanggal Bayar", style = MaterialTheme.typography.h6)

        // Ambil tanggalPinjam (hanya satu tanggal) dari hutangState
        val tanggalPinjam = hutangState?.tanggalPinjam ?: ""
        // Ubah menjadi list dengan satu elemen untuk dikirim ke CalendarView
        val tanggalPinjamList = if (tanggalPinjam.isNotEmpty()) listOf(tanggalPinjam) else emptyList()
        CalendarView(tanggalPinjamList)

        // Tampilkan informasi tanggal pinjam
        hutangState?.let { hutang ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tanggal Pinjam",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Tanggal: ${hutang.tanggalPinjam}")
                }
            }
        } ?: Text(text = "Tidak ada data hutang.", modifier = Modifier.padding(top = 8.dp))

        // Tombol Kembali
        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Kembali", color = Color.White)
        }
    }
}
