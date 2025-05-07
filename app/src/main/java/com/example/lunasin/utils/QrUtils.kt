package com.example.lunasin.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lunasin.utils.generateQRCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

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

@Preview(showBackground = true)
@Composable
fun QrCodeDialogButton(data: String = "lunasin://previewHutang?docId=preview123") {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth() // tombol selebar parent
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = "Generate QR Code",
            style = MaterialTheme.typography.labelLarge
        )
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("QR Code") },
            text = {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ✅ Gunakan scope maxWidth di sini
                    val qrSize = if (this.maxWidth < 300.dp) this.maxWidth - 32.dp else 250.dp

                    val qrBitmap = remember(data) {
                        generateQRCode(data)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(qrSize)
                        )
                    }
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
