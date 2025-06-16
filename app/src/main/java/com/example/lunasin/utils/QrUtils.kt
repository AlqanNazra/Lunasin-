package com.example.lunasin.utils

// DIUBAH KE M3: Mengganti semua import ke material3
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * DIUBAH KE M3: Fungsi diubah agar menerima warna dari tema Compose.
 * Ini membuat QR Code bisa beradaptasi dengan mode terang/gelap.
 */
fun generateQRCode(
    content: String,
    size: Int = 512,
    qrColor: Color,
    backgroundColor: Color
): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    val qrColorInt = qrColor.toArgb()
    val backgroundColorInt = backgroundColor.toArgb()

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) qrColorInt else backgroundColorInt)
        }
    }
    return bitmap
}

/**
 * DIUBAH KE M3: Menggunakan Button dan AlertDialog dari Material 3.
 */
@Composable
fun QrCodeDialogButton(data: String) {
    var showDialog by remember { mutableStateOf(false) }

    // Warna diambil dari tema saat ini
    val qrColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.surface

    Button(onClick = { showDialog = true }) {
        Text("Tampilkan QR Code")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pindai QR Code Ini") },
            text = {
                val qrBitmap = remember(data, qrColor, backgroundColor) {
                    generateQRCode(
                        content = data,
                        qrColor = qrColor,
                        backgroundColor = backgroundColor
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(8.dp)
                    )
                    Text("Pindai untuk melihat detail utang.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

/**
 * DIUBAH KE M3: Contoh layar penuh yang dibungkus Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQrScreen(docId: String, onBack: () -> Unit) {
    val qrData = "lunasin://previewHutang?docId=$docId"

    // Warna diambil dari tema saat ini
    val qrColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.surface

    val qrBitmap = remember(qrData, qrColor, backgroundColor) {
        generateQRCode(
            content = qrData,
            qrColor = qrColor,
            backgroundColor = backgroundColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code Utang") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pindai QR Code ini untuk melihat detail utang")
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code Hutang",
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(qrData, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}