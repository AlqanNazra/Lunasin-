package com.example.lunasin.Frontend.UI.Inputhutang

// DIUBAH KE M3: Import diganti ke Material 3
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class) // DIUBAH KE M3: Diperlukan untuk TopAppBar
@Composable
fun ListHutangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    val hutangList by hutangViewModel.hutangList.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("ListHutangScreen", "Memanggil ambilDataHutang untuk userId: $userId")
            hutangViewModel.ambilDataHutang(userId)
        } else {
            Log.e("ListHutangScreen", "User ID tidak ditemukan")
        }
    }

    Scaffold(
        topBar = {
            // DIUBAH KE M3: TopAppBar menggunakan parameter M3
            TopAppBar(
                title = { Text("Laporan Hutang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Menggunakan warna dari tema M3
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home_screen") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            // DIUBAH KE M3: FloatingActionButton menggunakan warna dari colorScheme
            FloatingActionButton(
                onClick = { navController.navigate("input_hutang_teman_screen") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp) // Menambahkan padding horizontal
        ) {
            if (hutangList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada data hutang.", modifier = Modifier.padding(16.dp))
                }
                Log.e("ListHutangScreen", "hutangList kosong, tidak ada data yang ditampilkan!")
            } else {
                hutangList.forEach { hutang ->
                    HutangItem(hutang, navController, hutangViewModel)
                }
            }
        }
    }
}

@Composable
fun HutangItem(hutang: Hutang, navController: NavHostController, hutangViewModel: HutangViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // DIUBAH KE M3: Card menggunakan parameter M3
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // elevation diatur di sini
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
            Text("Nominal: ${hutang.nominalpinjaman}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID Hutang: ${hutang.docId}", fontSize = 12.sp, color = Color.Gray) // Ukuran font diperkecil
                IconButton(
                    modifier = Modifier.size(24.dp), // Ukuran icon diperkecil
                    onClick = {
                        clipboardManager.setText(AnnotatedString(hutang.docId))
                        Toast.makeText(context, "ID Hutang disalin!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    Log.d("ListHutangScreen", "Button diklik, docId: ${hutang.docId}")
                    if (hutang.docId.isNotEmpty()) {
                        navController.navigate("preview_hutang/${hutang.docId}")
                    } else {
                        Log.e("ListHutangScreen", "Error: docId kosong!")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lihat Detail")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DIUBAH KE M3: Button menggunakan parameter M3
            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    hutangViewModel.hapusHutang(hutang.docId) {
                        hutangViewModel.ambilDataHutang(userId)
                    }
                    Toast.makeText(context, "Hutang berhasil dihapus", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hapus Hutang", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}