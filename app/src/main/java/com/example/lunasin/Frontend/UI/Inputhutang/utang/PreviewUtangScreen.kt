package com.example.lunasin.Frontend.UI.Inputhutang.utang

// DIUBAH KE M3: Mengganti semua import ke material3
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.utils.formatRupiah
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator as hutangca

// DIUBAH KE M3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewUtangScreen(
    viewModel: HutangViewModel,
    navController: NavController,
    docId: String,
    userId: String
) {
    Log.d("PREVIEW_SCREEN", "docId: $docId, userId: $userId")
    LaunchedEffect(docId) {
        if (docId.isNotEmpty()) {
            viewModel.getHutangById(docId)
        }
    }

    val hutang by viewModel.hutangState.collectAsState()

    // DIUBAH KE M3: Menggunakan Scaffold M3
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Utang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "Nama: ${hutang?.namapinjaman ?: "Data Kosong"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Nominal: ${hutang?.nominalpinjaman?.let { formatRupiah(it) } ?: "Rp0,00"}")
                    Text(text = "Periode: ${hutang?.lamaPinjaman} Bulan")
                    Text(text = "Dari: ${hutang?.tanggalPinjam} - ${hutang?.tanggalBayar}")

                    Spacer(modifier = Modifier.height(16.dp))

                    // DIUBAH KE M3: Menggunakan Card M3
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Denda Bila Telat: ${hutangca.formatRupiah(hutang?.totalHutang ?: 0.0)}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Catatan:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 100.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            Text(text = hutang?.catatan ?: "Tidak ada catatan")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Mendorong tombol ke bawah

                    // DIUBAH KE M3: Tombol disusun dalam Column dan menggunakan warna tema
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hutang?.id_penerima.isNullOrEmpty()) {
                            Button(
                                onClick = {
                                    hutang?.docId?.let { hutangId ->
                                        viewModel.klaimHutang(hutangId, userId)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Klaim Hutang Ini")
                            }
                        } else {
                            // Cek apakah yang klaim adalah user saat ini
                            if (hutang?.id_penerima == userId) {
                                Text("Hutang ini sudah Anda klaim.", textAlign = TextAlign.Center)
                            } else {
                                Text("Sudah Diklaim oleh orang lain.", textAlign = TextAlign.Center)
                            }
                        }

                        Button(
                            onClick = {
                                if (docId.isNotEmpty()) {
                                    navController.navigate("tanggalTempo/$docId")
                                } else {
                                    Log.e("LihatJatuhTempo", "docId NULL atau kosong")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lihat Jadwal Cicilan")
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Menggunakan OutlinedButton untuk aksi yang tidak sepenting "Klaim"
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kembali")
                        }
                    }
                }
            }
        }
    }
}