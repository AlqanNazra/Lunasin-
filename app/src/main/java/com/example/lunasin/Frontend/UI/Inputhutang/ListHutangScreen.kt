package com.example.lunasin.Frontend.UI.Inputhutang

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.google.firebase.auth.FirebaseAuth

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
            TopAppBar(
                title = { Text("Laporan Hutang") },
                backgroundColor = Color.White,
                elevation = 4.dp,
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home_screen") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("input_hutang_teman_screen") },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (hutangList.isEmpty()) {
                Text("Belum ada data hutang.", modifier = Modifier.padding(16.dp))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID Hutang: ${hutang.docId}", fontSize = 14.sp, color = Color.Gray)
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(hutang.docId))
                        Toast.makeText(context, "ID Hutang disalin!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = Color.Blue
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

            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    hutangViewModel.hapusHutang(hutang.docId) {
                        hutangViewModel.ambilDataHutang(userId)
                    }
                    Toast.makeText(context, "Hutang berhasil dihapus", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hapus Hutang", color = Color.White)
            }
        }
    }
}
