package com.example.lunasin.Frontend.UI.Inputhutang.utang

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import com.google.firebase.auth.FirebaseAuth
import com.example.lunasin.utils.generateQRCode

@Composable
fun ListUtangScreen(hutangViewModel: HutangViewModel, navController: NavHostController) {
    var searchId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val hasilCari by hutangViewModel.hutangState.collectAsState()
    val hutangList by hutangViewModel.hutangList.collectAsState()

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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 🔍 Search Bar
            OutlinedTextField(
                value = searchId,
                onValueChange = { searchId = it },
                label = { Text("Cari berdasarkan ID Hutang") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Button(onClick = {
                context.startActivity(Intent(context, QrScannerActivity::class.java))
            }) {
                Text("Scan QR Hutang")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Cari Hutang")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔁 Hasil Pencarian
            hasilCari?.let { hutang ->
                Text(
                    "Hasil Pencarian:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                HutangItem(hutang = hutang, navController = navController)
                Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                val qrBitmap = remember(hutang.docId) {
                    generateQRCode("lunasin://previewHutang?docId=${hutang.docId}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Scan QR untuk klaim hutang:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Hutang",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(200.dp)
                )
            }


            if (hutangList.isEmpty()) {
                Text("Belum ada data hutang.", modifier = Modifier.padding(16.dp))
            } else {
                Text(
                    "Hutang Saya:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                hutangList.forEach { hutang ->
                    HutangItem(hutang = hutang, navController = navController)
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
            Text("Nominal: ${hutang.nominalpinjaman}", fontSize = 16.sp, color = Color.Blue)

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
