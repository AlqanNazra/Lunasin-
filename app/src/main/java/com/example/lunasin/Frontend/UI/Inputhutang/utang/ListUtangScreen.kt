package com.example.lunasin.Frontend.UI.Inputhutang.utang

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import kotlinx.coroutines.launch

val Black = Color(0xFF000000)

@Composable
fun ListUtangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    var searchId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val hasilCari by hutangViewModel.hutangState.collectAsState()
    val hutangSayaList by hutangViewModel.hutangSayaList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Ambil data hutang saya saat screen ditampilkan
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("ListUtangScreen", "Mengambil data hutang saya untuk userId: $userId")
            hutangViewModel.ambilHutangSaya(userId)
        } else {
            Log.e("ListUtangScreen", "User ID tidak ditemukan")
        }
    }

    // Amati perubahan hasilCari untuk menampilkan pesan
    LaunchedEffect(hasilCari) {
        if (isLoading) {
            isLoading = false
            hasilCari?.let { hutang ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Hutang ditemukan")
                }
            } ?: run {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Hutang tidak ditemukan")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background, // Menggunakan background dari tema (Grey99)
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Placeholder untuk membuka kamera
                    Toast.makeText(context, "Membuka kamera untuk scan QR Code", Toast.LENGTH_SHORT).show()
                    // Tambahkan logika untuk membuka kamera di sini
                    // Misalnya: menggunakan library seperti CameraX atau intent ke aplikasi kamera
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Scan QR Code",
                    modifier = Modifier.size(24.dp)
                )
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
                    text = "Daftar Hutang Saya",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lihat daftar hutang Anda di sini",
                    style = MaterialTheme.typography.labelMedium,
                    color = Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 🔍 Search Bar
                OutlinedTextField(
                    value = searchId,
                    onValueChange = { searchId = it },
                    label = { Text("Cari berdasarkan ID Hutang") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        if (searchId.isNotEmpty()) {
                            IconButton(onClick = { searchId = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus Pencarian",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Cari
                val isButtonEnabled = searchId.isNotBlank() && !isLoading
                Button(
                    onClick = {
                        if (searchId.isNotEmpty()) {
                            isLoading = true
                            hutangViewModel.getHutangById(searchId)
                            Log.d("SearchBar", "Mencari hutang dengan ID: $searchId")
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Masukkan ID Hutang")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    enabled = isButtonEnabled
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            "Cari Hutang",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔁 Hasil Pencarian
                hasilCari?.let { hutang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hasil Pencarian",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                hutangViewModel.clearHutangState()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Hapus Hasil Pencarian",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    HutangItem(hutang = hutang, navController = navController)
                    Divider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Daftar Hutang Saya
                Text(
                    "Hutang Saya",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (hutangSayaList.isEmpty()) {
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
                                "Belum ada data hutang.",
                                style = MaterialTheme.typography.labelMedium,
                                color = Black.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { navController.navigate("tambah_hutang") }) {
                                Text(
                                    "Tambah Hutang Sekarang",
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
                        items(hutangSayaList) { hutang ->
                            HutangItem(hutang = hutang, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HutangItem(hutang: Hutang, navController: NavHostController) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable {
                if (hutang.docId.isNotEmpty()) {
                    navController.navigate("preview_utang/${hutang.docId}")
                } else {
                    Log.e("ListUtangScreen", "Error: docId kosong!")
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    text = hutang.namapinjaman.firstOrNull()?.uppercase() ?: "H",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hutang.namapinjaman,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Nominal: Rp ${hutang.nominalpinjaman?.let { String.format("%,.0f", it) } ?: "0"}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "ID: ${hutang.docId}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, color = Black.copy(alpha = 0.7f))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(hutang.docId))
                    Toast.makeText(context, "ID Hutang disalin!", Toast.LENGTH_SHORT).show()
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
    }
}
