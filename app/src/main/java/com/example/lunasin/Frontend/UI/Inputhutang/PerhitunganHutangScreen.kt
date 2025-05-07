package com.example.lunasin.Frontend.UI.Inputhutang

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.material3.ButtonDefaults

@Composable
fun PerhitunganHutangScreen(hutangViewModel: HutangViewModel, navController: NavController) {
    val context = LocalContext.current
    var namaPinjaman by remember { mutableStateOf("") }
    var nominalPinjaman by remember { mutableStateOf("") }
    var denda by remember { mutableStateOf("") }
    var lamaPinjaman by remember { mutableStateOf("") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Masukkan Data",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF008D36)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Luna butuh info-info ini buat bisa catat hutang kamu",
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Nama Peminjam
        OutlinedTextField(
            value = namaPinjaman,
            onValueChange = { namaPinjaman = it },
            label = { Text("Nama Peminjam") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
        )

        // Input Nominal Pinjaman
        OutlinedTextField(
            value = nominalPinjaman,
            onValueChange = { nominalPinjaman = it },
            label = { Text("Nominal Pinjaman") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
        )

        // Input Tanggal Mulai Pinjaman
        OutlinedTextField(
            value = tanggalPinjam,
            onValueChange = {},
            label = { Text("Tanggal Mulai Pinjaman") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePicker { tanggalPinjam = it } }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pilih Tanggal",
                        tint = Color.Gray
                    )
                }
            },
            shape = RoundedCornerShape(8.dp)
        )

        // Input Denda Telat Bayar
        OutlinedTextField(
            value = denda,
            onValueChange = { denda = it },
            label = { Text("Denda Telat Bayar") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
        )

        // Input Periode
        OutlinedTextField(
            value = lamaPinjaman,
            onValueChange = { lamaPinjaman = it },
            label = { Text("Periode") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
        )

        // Input Catatan
        OutlinedTextField(
            value = catatan,
            onValueChange = { catatan = it },
            label = { Text("Catatan") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
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
                val totaldenda = denda.toDoubleOrNull()
                val lamaPinjamValue = lamaPinjaman.toIntOrNull()

                if (namaPinjaman.isEmpty() || pinjamanValue == null || totaldenda == null || lamaPinjamValue == null) {
                    Toast.makeText(context, "Input tidak valid! Masukkan angka yang benar.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                Log.d("InputHutangScreen", "Mengirim data ke Firestore...")

                hutangViewModel.hitungDanSimpanHutang(
                    hutangType = HutangViewModel.HutangType.PERHITUNGAN,
                    namaPinjaman, pinjamanValue, totaldenda, lamaPinjamValue, tanggalPinjam, catatan
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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C4B4),  // Warna latar belakang tombol
                contentColor = Color.White           // Warna teks/isi tombol
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        LaunchedEffect(navigateToPreview) {
            navigateToPreview?.let { docId ->
                showPopup = false
                delay(500)
                navController.navigate("perhitungan_preview_hutang/$docId")
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