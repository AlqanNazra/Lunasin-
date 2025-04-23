package com.example.lunasin.Frontend.UI.Inputhutang

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.lunasin.theme.Black

@Composable
fun ListPiutangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    val piutangSayaList by hutangViewModel.piutangSayaList.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("ListPiutangScreen", "Memanggil ambilPiutangSaya untuk userId: $userId")
            hutangViewModel.ambilPiutangSaya(userId)
        } else {
            Log.e("ListPiutangScreen", "User ID tidak ditemukan")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background, // Menggunakan background dari tema (Grey99)
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("input_hutang_teman_screen") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header dengan background putih dan garis tipis
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("home_screen") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Judul
                Text(
                    text = "List Piutang Saya",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lihat daftar piutang Anda di sini",
                    style = MaterialTheme.typography.labelMedium,
                    color = Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Daftar Piutang Saya
                if (piutangSayaList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tidak ada data",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Belum ada data piutang.",
                                style = MaterialTheme.typography.labelMedium,
                                color = Black.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { navController.navigate("input_hutang_teman_screen") }) {
                                Text(
                                    "Tambah Piutang Sekarang",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(piutangSayaList) { piutang ->
                            PiutangItem(piutang, navController, hutangViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PiutangItem(piutang: Hutang, navController: NavHostController, hutangViewModel: HutangViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable {
                if (piutang.docId.isNotEmpty()) {
                    navController.navigate("preview_hutang/${piutang.docId}")
                } else {
                    Log.e("ListPiutangScreen", "Error: docId kosong!")
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon dekoratif di sisi kiri
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = piutang.namapinjaman.firstOrNull()?.uppercase() ?: "P",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = piutang.namapinjaman,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Nominal: Rp ${piutang.nominalpinjaman?.let { String.format("%,.0f", it) } ?: "0"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "ID: ${piutang.docId}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Black.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "Tanggal Pinjam: ${piutang.tanggalPinjam?.ifEmpty { "Belum ditentukan" } ?: "Belum ditentukan"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Black.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "Tanggal Bayar: ${piutang.tanggalBayar?.ifEmpty { "Belum ditentukan" } ?: "Belum ditentukan"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Black.copy(alpha = 0.7f))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(piutang.docId))
                        Toast.makeText(context, "ID Piutang disalin!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        Log.d("ListPiutangScreen", "Button Lihat Detail diklik, docId: ${piutang.docId}")
                        if (piutang.docId.isNotEmpty()) {
                            navController.navigate("preview_hutang/${piutang.docId}")
                        } else {
                            Log.e("ListPiutangScreen", "Error: docId kosong!")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Lihat Detail", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                        hutangViewModel.hapusHutang(piutang.docId) {
                            hutangViewModel.ambilPiutangSaya(userId)
                        }
                        Toast.makeText(context, "Piutang berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Hapus", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}