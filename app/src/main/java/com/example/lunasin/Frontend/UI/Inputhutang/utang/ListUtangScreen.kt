package com.example.lunasin.Frontend.UI.Inputhutang.utang

// DIUBAH KE M3: Mengganti semua import ke material3
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.UI.Inputhutang.Qrcode.QrScannerActivity
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.utils.generateQRCode

// DIUBAH KE M3: Diperlukan untuk TopAppBar M3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListUtangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    var searchId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val hasilCari by hutangViewModel.hutangState.collectAsState()
    val hutangList by hutangViewModel.hutangList.collectAsState()

    // DIUBAH KE M3: Menggunakan Scaffold M3
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Hutang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Menggunakan warna dari tema M3
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home_screen") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // DIUBAH KE M3: Menggunakan komponen M3
            OutlinedTextField(
                value = searchId,
                onValueChange = { searchId = it },
                label = { Text("Cari berdasarkan ID Hutang") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, QrScannerActivity::class.java))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Scan QR")
                }
                Button(
                    onClick = {
                        if (searchId.isNotEmpty()) {
                            hutangViewModel.getHutangById(searchId)
                            Log.d("SearchBar", "Mencari hutang dengan ID: $searchId")
                        } else {
                            Toast.makeText(context, "Masukkan ID Hutang", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cari")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hasil Pencarian
            hasilCari?.let { hutang ->
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Hasil Pencarian:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HutangItem(hutang = hutang, navController = navController) // HutangItem juga harus M3
                    Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

                    val qrBitmap = remember(hutang.docId) {
                        generateQRCode(
                            "lunasin://previewHutang?docId=${hutang.docId}",
                            size = TODO(),
                            qrColor = TODO(),
                            backgroundColor = TODO()
                        )
                    }
                    Text("Scan QR untuk klaim hutang:", fontWeight = FontWeight.Bold)
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Hutang",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(200.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            // Daftar Semua Hutang
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (hutangList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data hutang.")
                    }
                } else {
                    Text(
                        "Semua Hutang Saya:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    hutangList.forEach { hutang ->
                        HutangItem(hutang = hutang, navController = navController)
                    }
                }
            }
        }
    }
}

// DIUBAH KE M3: Helper Composable juga harus M3
@Composable
fun HutangItem(hutang: Hutang, navController: NavHostController) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            Text(
                "Nominal: ${hutang.nominalpinjaman}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID Hutang: ${hutang.docId}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(
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
                    Log.d("ListUtangScreen", "Button diklik, docId: ${hutang.docId}")
                    if (hutang.docId.isNotEmpty()) {
                        navController.navigate("preview_utang/${hutang.docId}")
                    } else {
                        Log.e("ListUtangScreen", "Error: docId kosong!")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lihat Detail")
            }
        }
    }
}