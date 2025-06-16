package com.example.lunasin.Frontend.UI.Inputhutang

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemanHutangScreen(hutangViewModel: HutangViewModel, navController: NavController) {
    val context = LocalContext.current
    var namaPinjaman by remember { mutableStateOf("") }
    var nominalPinjaman by remember { mutableStateOf("") }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catat Utang Biasa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Masukkan Data", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Text("Lengkapi info di bawah untuk mencatat utang biasa.", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = namaPinjaman,
                onValueChange = { namaPinjaman = it },
                label = { Text("Nama Pinjaman") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nominalPinjaman,
                onValueChange = { if (it.all(Char::isDigit)) nominalPinjaman = it },
                label = { Text("Nominal Pinjaman") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tanggalPinjam,
                onValueChange = {},
                label = { Text("Tanggal Pinjaman") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePicker { tanggalPinjam = it } }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (tanggalPinjam == "Pilih Tanggal") {
                        Toast.makeText(context, "Harap pilih tanggal pinjaman!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val pinjamanValue = nominalPinjaman.toDoubleOrNull()
                    if (namaPinjaman.isBlank() || pinjamanValue == null) {
                        Toast.makeText(context, "Nama dan Nominal harus diisi dengan benar.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    Log.d("TemanHutangScreen", "Mengirim data ke Firestore...")

                    hutangViewModel.hitungDanSimpanHutang(
                        hutangType = HutangViewModel.HutangType.TEMAN,
                        namapinjaman = namaPinjaman,
                        nominalpinjaman = pinjamanValue,
                        // DIUBAH: Menggunakan nama parameter yang benar 'tanggalPinjamString'
                        tanggalPinjamString = tanggalPinjam,
                        catatan = catatan,
                        bunga = 0.0, // Tidak dipakai untuk tipe TEMAN
                        lamaPinjam = 0, // Tidak dipakai untuk tipe TEMAN
                    ) { success, docId ->
                        isLoading = false
                        if (success && docId != null) {
                            popupMessage = "Hutang berhasil disimpan!"
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Simpan Hutang")
                }
            }

            LaunchedEffect(navigateToPreview) {
                navigateToPreview?.let { docId ->
                    delay(500)
                    navController.navigate("teman_preview_hutang/$docId")
                    navigateToPreview = null
                }
            }

            if (showPopup && !isLoading) {
                AlertDialog(
                    onDismissRequest = { showPopup = false },
                    title = { Text("Status") },
                    text = { Text(popupMessage) },
                    confirmButton = {
                        TextButton(onClick = { showPopup = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}