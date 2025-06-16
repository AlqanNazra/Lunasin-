package com.example.lunasin.Frontend.UI.Inputhutang

// DIUBAH KE M3: Mengganti semua import ke material3
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.utils.CalendarView

@OptIn(ExperimentalMaterial3Api::class) // DIUBAH KE M3
@Composable
fun TanggalTempoScreen(viewModel: HutangViewModel, navController: NavController, docId: String) {
    val hutangState by viewModel.hutangState.collectAsState()

    LaunchedEffect(docId) {
        viewModel.getHutangById(docId)
    }

    // DIUBAH KE M3: Menggunakan Scaffold untuk struktur layar yang konsisten
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Cicilan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // DIUBAH KE M3: Menggunakan tipografi M3
            Text(
                text = "Kalender Jatuh Tempo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Menampilkan kalender jika data sudah ada
            val jatuhTempoList = hutangState?.listTempo?.map { it.tanggalTempo } ?: emptyList()
            if (jatuhTempoList.isNotEmpty()) {
                CalendarView(jatuhTempoList)
            } else {
                // Menampilkan loading atau pesan kosong
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Detail Cicilan:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            hutangState?.let { hutang ->
                val listTempo = hutang.listTempo ?: emptyList()

                if (listTempo.isEmpty()) {
                    Text(text = "Tidak ada jadwal cicilan.", modifier = Modifier.padding(top = 8.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f) // Agar daftar bisa scroll dan tombol tetap di bawah
                    ) {
                        items(listTempo) { tempo ->
                            // DIUBAH KE M3: Menggunakan Card M3
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pembayaran ${tempo.angsuranKe}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = "Tanggal Tempo: ${tempo.tanggalTempo}")
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Memuat data...")
            }

            // DIUBAH KE M3: Menggunakan Button M3 dengan warna dari tema
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Kembali", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}