package com.example.lunasin.Frontend.UI.Hutang.Piutang

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Backend.Model.HutangType // Impor HutangType secara langsung
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.theme.Black
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriusPiutangScreen(
    hutangViewModel: HutangViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var namaPinjaman by remember { mutableStateOf("") }
    var nominalPinjaman by remember { mutableStateOf("") }
    var tanggalPinjam by remember { mutableStateOf("Pilih Tanggal") }
    var tanggalJatuhTempo by remember { mutableStateOf("Pilih Tanggal Jatuh Tempo") }
    var bunga by remember { mutableStateOf("") }
    var lamaPinjaman by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var navigateToPreview by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }
    var nominalValue by remember { mutableStateOf("") }


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
                title = {
                    Text(
                        text = "Input Piutang Serius",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Masukkan Data",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Luna butuh info-info ini buat bisa catat piutang kamu",
                style = MaterialTheme.typography.bodyMedium,
                color = Black.copy(alpha = 0.7f)
            )

            OutlinedTextField(
                value = namaPinjaman,
                onValueChange = { namaPinjaman = it },
                label = { Text("Nama Pemberi Pinjaman") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = nominalPinjaman,
                onValueChange = { input ->
                    // Hapus titik dulu untuk ambil angka mentah
                    val cleanInput = input.replace(".", "").filter { it.isDigit() }

                    nominalValue = cleanInput

                    // Format angka ke format 100.000
                    nominalPinjaman = if (cleanInput.isNotEmpty()) {
                        NumberFormat.getInstance(Locale("in", "ID")).format(cleanInput.toLong())
                    } else {
                        ""
                    }
                },
                label = { Text("Nominal Piutang") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

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
                            contentDescription = "Pilih Tanggal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = bunga,
                onValueChange = { bunga = it },
                label = { Text("Bunga (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = lamaPinjaman,
                onValueChange = { lamaPinjaman = it },
                label = { Text("Lama Pinjaman (Bulan)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Kembali", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = {
                        if (tanggalPinjam == "Pilih Tanggal") {
                            Toast.makeText(context, "Harap pilih tanggal pinjaman dan jatuh tempo!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val pinjamanValue = nominalPinjaman.replace(".", "").replace(",", "").toDoubleOrNull()
                        val bungaValue = bunga.toDoubleOrNull()
                        val lamaPinjamanValue = lamaPinjaman.toIntOrNull()

                        if (namaPinjaman.isEmpty() || pinjamanValue == null || bungaValue == null || lamaPinjamanValue == null) {
                            Toast.makeText(context, "Input tidak valid! Masukkan angka yang benar.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        Log.d("SeriusPiutangScreen", "Mengirim data ke Firestore...")

                        hutangViewModel.hitungDanSimpanHutang(
                            type = "Piutang", // Menambahkan parameter type
                            hutangType = HutangType.SERIUS, // Menggunakan HutangType langsung
                            namapinjaman = namaPinjaman,
                            nominalpinjaman = pinjamanValue,
                            bunga = bungaValue,
                            lamaPinjam = lamaPinjamanValue,
                            tanggalPinjam = tanggalPinjam,
                            tanggalJatuhTempo = tanggalJatuhTempo,
                            catatan = catatan
                        ) { success, docId ->
                            isLoading = false
                            if (success && docId != null) {
                                popupMessage = "Piutang berhasil disimpan!"
                                showPopup = true
                                navigateToPreview = docId
                            } else {
                                popupMessage = "Gagal menyimpan piutang!"
                                showPopup = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Confirm", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Popup Notifikasi
            if (showPopup) {
                AlertDialog(
                    onDismissRequest = { showPopup = false },
                    title = { Text("Status") },
                    text = { Text(popupMessage) },
                    confirmButton = {
                        TextButton(onClick = { showPopup = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surface // Mengganti backgroundColor menjadi containerColor
                )
            }
        }
    }

    LaunchedEffect(navigateToPreview) {
        navigateToPreview?.let { docId ->
            showPopup = false
            delay(500)
            navController.navigate("piutang_serius_preview/$docId")
            navigateToPreview = null
        }
    }
}