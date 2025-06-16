package com.example.lunasin.Frontend.UI.Inputhutang

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.utils.QrCodeDialogButton
import com.example.lunasin.utils.formatRupiah
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator as hutangca

@OptIn(ExperimentalMaterial3Api::class) // DIUBAH KE M3: Diperlukan untuk TopAppBar M3
@Composable
fun PerhitunganPreviewHutangScreen(
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

    // DIUBAH KE M3: Menggunakan Scaffold M3
    Scaffold(
        topBar = {
            // DIUBAH KE M3: Menggunakan TopAppBar M3 dengan warna dari tema
            TopAppBar(
                title = { Text("Preview Hutang") },
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
                CircularProgressIndicator() // DIUBAH KE M3: Menggunakan CircularProgressIndicator M3
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), // Menambahkan scroll
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

                    // DIUBAH KE M3: Menggunakan Card M3
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Denda Bila Telat:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = hutangca.formatRupiah(hutang?.totalHutang ?: 0.0),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Catatan:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Card(
                        shape = RoundedCornerShape(8.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Menggunakan Column agar tombol tersusun rapi
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (docId.isNotEmpty()) {
                                    navController.navigate("tanggalTempo/$docId")
                                } else {
                                    Log.e("LihatJatuhTempo", "docId NULL atau kosong")
                                }
                            },
                            // DIUBAH KE M3: Menggunakan warna tema untuk konsistensi
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lihat Jatuh Tempo")
                        }

                        hutang?.let { data ->
                            QrCodeDialogButton(data = "lunasin://previewHutang?docId=${data.docId}")
                        }

                        Button(
                            onClick = { navController.navigate("home_screen") { popUpTo(0) } }, // Kembali ke home dan hapus backstack
                            // DIUBAH KE M3: Warna secondary untuk aksi konfirmasi
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Selesai")
                        }

                        Button(
                            onClick = { navController.popBackStack() },
                            // DIUBAH KE M3: Warna error untuk aksi kembali/batal
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
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