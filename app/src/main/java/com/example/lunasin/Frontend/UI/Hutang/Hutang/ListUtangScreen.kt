package com.example.lunasin.Frontend.UI.Hutang.Hutang

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.theme.Black
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.lunasin.utils.Notifikasi.NotificationReceiver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListUtangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    var searchId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val recentSearch by hutangViewModel.recentSearch.collectAsState()
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

    // Amati perubahan recentSearch untuk menampilkan pesan
    LaunchedEffect(recentSearch) {
        if (isLoading) {
            isLoading = false
            recentSearch?.let { hutang ->
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val options = GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = GmsBarcodeScanning.getClient(context, options)
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            val docId = barcode.rawValue?.trim()
                            if (!docId.isNullOrEmpty()) {
                                Log.d("QRScan", "Scanned docId: $docId") // Tambahkan log di sini
                                coroutineScope.launch {
                                    isLoading = true
                                    hutangViewModel.getHutangById(docId) { errorMessage ->
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                    // Setelah data dimuat, navigasi ke layar preview sesuai hutangType
                                    hutangViewModel.hutangState.value?.let { hutang ->
                                        when (hutang.hutangType) {
                                            HutangType.TEMAN -> navController.navigate("hutang_teman_preview/$docId")
                                            HutangType.PERHITUNGAN -> navController.navigate("hutang_perhitungan_preview/$docId")
                                            else -> {
                                                Log.e("ListUtangScreen", "Tipe hutang tidak dikenali: ${hutang.hutangType}")
                                                navController.navigate("hutang_teman_preview/$docId") // Fallback
                                            }
                                        }
                                    } ?: run {
                                        Log.e("QRScan", "Dokumen tidak ditemukan untuk docId: $docId") // Log error
                                        snackbarHostState.showSnackbar("Dokumen tidak ditemukan!")
                                    }
                                    isLoading = false
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Gagal memindai QR Code!")
                                }
                            }
                        }
                        .addOnFailureListener {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Gagal memulai pemindaian: ${it.message}")
                            }
                        }
                        .addOnCanceledListener {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pemindaian dibatalkan!")
                            }
                        }
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
                            hutangViewModel.getHutangById(searchId) { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            Log.d("SearchBar", "Mencari hutang dengan ID: $searchId")
                            // Pastikan isLoading diatur ulang setelah proses selesai
                            coroutineScope.launch {
                                // Tunggu hingga getHutangById selesai (opsional, tergantung kebutuhan)
                                delay(2000) // Timeout sementara untuk debugging
                                isLoading = false
                                hutangViewModel.hutangState.value?.let { hutang ->
                                    when (hutang.hutangType) {
                                        HutangType.TEMAN -> navController.navigate("hutang_teman_preview/$searchId")
                                        HutangType.PERHITUNGAN -> navController.navigate("hutang_perhitungan_preview/$searchId")
                                        else -> navController.navigate("hutang_teman_preview/$searchId")
                                    }
                                } ?: run {
                                    snackbarHostState.showSnackbar("Dokumen tidak ditemukan!")
                                }
                            }
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
                @OptIn(ExperimentalMaterial3Api::class)
                Spacer(modifier = Modifier.height(24.dp))
                val timePickerState = rememberTimePickerState()
                var showTimePicker by remember { mutableStateOf(false) }

//                Button(
//                    onClick = { showTimePicker = true },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                ) {
//                    Text("Set Notifikasi Harian")
//                }

                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        title = { Text("Pilih Waktu Notifikasi") },
                        text = {
                            TimePicker(state = timePickerState)
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                hutangViewModel.scheduleDailyNotification(
                                    context,
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                Toast.makeText(
                                    context,
                                    "Notifikasi dijadwalkan setiap jam ${timePickerState.hour}:${timePickerState.minute}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showTimePicker = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Dokumen Terakhir Dibuka (Recent Search)
                val currentUserId = hutangViewModel.currentUserId
                recentSearch?.let { hutang ->
                    // Hanya tampilkan jika id_penerima == currentUserId (hutang) dan bukan piutang (userId != currentUserId)
                    if (hutang.id_penerima == currentUserId.value && hutang.userId != currentUserId.value) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Dokumen Terakhir Dibuka",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            IconButton(
                                onClick = {
                                    hutangViewModel.clearRecentSearch() // Hapus recent search
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Recent Search",
                                    tint = MaterialTheme.colorScheme.error
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
                            TextButton(onClick = { navController.navigate("input_hutang_teman_screen") }) {
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
                    // Navigasi berdasarkan hutangType
                    when (hutang.hutangType) {
                        HutangType.TEMAN -> navController.navigate("hutang_teman_preview/${hutang.docId}")
                        HutangType.PERHITUNGAN -> navController.navigate("hutang_perhitungan_preview/${hutang.docId}")
                        else -> {
                            Log.e("ListUtangScreen", "Tipe hutang tidak dikenali: ${hutang.hutangType}")
                            navController.navigate("hutang_teman_preview/${hutang.docId}") // Fallback ke Teman
                        }
                    }
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
                    text = "Total Hutang: Rp ${String.format("%,.0f", hutang.totalHutang)}",
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