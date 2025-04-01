package com.example.lunasin.Frontend.UI.Inputhutang

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun SeriusHutangScreen(hutangViewModel: HutangViewModel, navController: NavController) {
    val context = LocalContext.current
    var namaPinjaman by remember { mutableStateOf("") }
    var nominalPinjaman by remember { mutableStateOf("") }
    var bunga by remember { mutableStateOf("") }
    var periodePinjaman by remember { mutableStateOf("") }
    var tanggalPinjam by remember { mutableStateOf("Pilih Tanggal") }
    var navigateToPreview by remember { mutableStateOf<String?>(null) }
    var catatan by remember { mutableStateOf("") }


    var isLoading by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePicker = { onDateSelected: (String) -> Unit ->
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Masukkan Data", fontSize = 20.sp, color = Color(0xFF008D36))
        Text("Luna butuh info-info ini buat bisa catat hutang kamu", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Input Nama Pinjaman
        OutlinedTextField(
            value = namaPinjaman,
            onValueChange = { namaPinjaman = it },
            label = { Text("Nama Pinjaman") },
            modifier = Modifier.fillMaxWidth()
        )

        // Input Nominal Pinjaman
        OutlinedTextField(
            value = nominalPinjaman,
            onValueChange = { nominalPinjaman = it },
            label = { Text("Nominal Pinjaman") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Input Bunga
        OutlinedTextField(
            value = bunga,
            onValueChange = { bunga = it },
            label = { Text("Bunga per Bulan (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Input Periode Pinjaman
        OutlinedTextField(
            value = periodePinjaman,
            onValueChange = { periodePinjaman = it },
            label = { Text("Periode Pinjaman (Bulan)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = tanggalPinjam,
            onValueChange = {},
            label = { Text("Tanggal Mulai Pinjaman") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePicker { tanggalPinjam = it } }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pilih Tanggal"
                    )
                }
            }
        )

        // Input Catatan
        OutlinedTextField(
            value = catatan,
            onValueChange = { catatan = it },
            label = { Text("Catatan") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Confirm
        Button(
            onClick = {
                if (tanggalPinjam == "Pilih Tanggal") {
                    Toast.makeText(context, "Harap pilih tanggal pinjaman!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val pinjamanValue = nominalPinjaman.toDoubleOrNull()
                val bungaValue = bunga.toDoubleOrNull()
                val lamaPinjamValue = periodePinjaman.toIntOrNull()

                if (namaPinjaman.isEmpty() || pinjamanValue == null || bungaValue == null || lamaPinjamValue == null) {
                    Toast.makeText(context, "Input tidak valid! Masukkan angka yang benar.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                Log.d("InputHutangScreen", "Mengirim data ke Firestore...")

                hutangViewModel.hitungDanSimpanHutang_Serius(
                    namaPinjaman, pinjamanValue, bungaValue, lamaPinjamValue, tanggalPinjam, catatan
                ) { success, docId ->
                    isLoading = false
                    if (success && docId != null) {
                        popupMessage = "Hutang berhasil disimpan! Data jatuh tempo juga dibuat."
                        showPopup = true
                        navigateToPreview = docId
                    } else {
                        popupMessage = "Gagal menyimpan hutang!"
                        showPopup = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
            } else {
                Text("Confirm")
            }
        }


        LaunchedEffect(navigateToPreview) {
            navigateToPreview?.let { docId ->
                showPopup = false
                delay(500)
                navController.navigate("preview_hutang/$docId")  // Navigasi ke Preview Hutang
                navigateToPreview = null
            }
        }


        // Dialog Loading
        if (isLoading) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Mengirim Data...") },
                text = {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Harap tunggu...")
                    }
                },
                confirmButton = { }
            )
        }

        // Popup Notifikasi
        if (showPopup) {
            AlertDialog(
                onDismissRequest = { showPopup = false },
                title = { Text("Status") },
                text = { Text(popupMessage) },
                confirmButton = {
                    Button(onClick = { showPopup = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
