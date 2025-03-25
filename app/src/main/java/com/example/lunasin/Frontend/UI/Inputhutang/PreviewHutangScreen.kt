package com.example.lunasin.Frontend.UI.Inputhutang

import android.util.Log
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
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.utils.formatRupiah
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PreviewHutangScreen(
    viewModel: HutangViewModel,
    navController: NavController,
    docId: String
) {
    LaunchedEffect(docId) {
        if (docId.isNotEmpty()) {
            viewModel.getHutangById(docId)
        }
    }

    val hutang by viewModel.hutangState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Hutang") },
                backgroundColor = Color.Blue,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (hutang == null) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nama: ${hutang?.namapinjaman ?: "Data Kosong"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(text = "Nominal: ${hutang?.nominalpinjaman?.let { formatRupiah(it) } ?: "Rp0,00"}")
                    Text(text = "Periode: ${hutang?.lamaPinjaman} Bulan")
                    Text(text = "Dari: ${hutang?.tanggalPinjam} - ${hutang?.tanggalBayar}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Total Bunga: ${viewModel.formatRupiah(hutang?.totalbunga ?: 0.0)}")
                            Text(text = "Total Hutang: ${viewModel.formatRupiah(hutang?.totalHutang ?: 0.0)}")
                            Text(text = "Cicilan per Bulan: ${viewModel.formatRupiah(hutang?.totalcicilan ?: 0.0)}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { navController.popBackStack() }, // Kembali ke layar sebelumnya
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red) // Ganti containerColor -> backgroundColor
                        ) {
                            Text("Kembali", color = Color.White)
                        }

                        Button(
                            onClick = {
                                navController.navigate("laporan_hutang_screen") // Hanya pindah ke halaman home
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green) // Warna tombol tetap hijau
                        ) {
                            Text("Confirm", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (docId.isNotEmpty()) {
                                    navController.navigate("tanggalTempo/$docId")
                                } else {
                                    Log.e("LihatJatuhTempo", "docId NULL atau kosong")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue) // Ganti containerColor -> backgroundColor
                        ) {
                            Text("Lihat Jatuh Tempo", color = Color.White)
                        }

                    }
                }
            }
        }
    }
}
