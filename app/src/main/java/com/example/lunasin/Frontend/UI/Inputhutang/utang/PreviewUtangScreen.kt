package com.example.lunasin.Frontend.UI.Inputhutang.utang

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
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator as hutangca

@Composable
fun PerhitunganPreviewHutangScreen(
    viewModel: HutangViewModel,
    navController: NavController,
    docId: String,
    userId: String // Tambahkan ini agar bisa klaim hutang
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
                            Text(text = "Denda Bila Telat: ${hutangca.formatRupiah(hutang?.totalHutang ?: 0.0)}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Menampilkan Catatan
                    Text(text = "Catatan:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            Text(text = hutang?.catatan ?: "Tidak ada catatan")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                        ) {
                            Text("Kembali", color = Color.White)
                        }

                        // Tombol Klaim Hutang
                        if (hutang?.id_penerima == null) {
                            Button(onClick = {
                                hutang?.docId?.let { hutangId ->
                                    viewModel.klaimHutang(hutangId, userId)
                                }
                            }) {
                                Text("Klaim Hutang")
                            }
                        } else {
                            Text("Sudah Diklaim oleh ${hutang?.id_penerima}")
                        }

                        Button(
                            onClick = {
                                if (docId.isNotEmpty()) {
                                    navController.navigate("tanggalTempo/$docId")
                                } else {
                                    Log.e("LihatJatuhTempo", "docId NULL atau kosong")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                        ) {
                            Text("Lihat Jatuh Tempo", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
