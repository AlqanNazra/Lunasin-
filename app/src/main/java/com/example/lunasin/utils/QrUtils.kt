package com.example.lunasin.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lunasin.utils.generateQRCode

fun generateQRCode(content: String, size: Int = 512): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

@Composable
fun QrCodeDialogButton(data: String) {
    var showDialog by remember { mutableStateOf(false) }

    Button(onClick = { showDialog = true }) {
        Text("Generate QR Code")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("QR Code") },
            text = {
                val qrBitmap = remember(data) {
                    generateQRCode(data)
                }
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp)
                    )
                    Text("Scan untuk melihat detail hutang.")
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun GenerateQrScreen(docId: String) {
    val context = LocalContext.current
    val qrData = "https://lunasin.app/hutang?docId=$docId"
    val qrBitmap = generateQRCode(qrData)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scan untuk lihat detail hutang")
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "QR Code Hutang",
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(qrData, fontSize = 12.sp)
    }
}
