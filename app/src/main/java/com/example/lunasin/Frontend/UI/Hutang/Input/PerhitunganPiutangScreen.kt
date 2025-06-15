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
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.theme.Black
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerhitunganPiutangScreen(
    hutangViewModel: HutangViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var namaPinjaman by remember { mutableStateOf("") }
    var nominalPinjaman by remember { mutableStateOf("") }
    var tanggalPinjam by remember { mutableStateOf("Pilih Tanggal") }
    var tanggalJatuhTempo by remember { mutableStateOf("Pilih Tanggal Jatuh Tempo") }
    var totalDenda by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var navigateToPreview by remember { mutableStateOf<String?>(null) }
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
                title = {
                    Text(
                        text = "Input Piutang Perhitungan",
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
                label = { Text("Nama Penerima Pinjaman") },
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
                    val cleanInput = input.replace(".", "").filter { it.isDigit() }
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
                value = tanggalJatuhTempo,
                onValueChange = {},
                label = { Text("Tanggal Jatuh Tempo Pembayaran") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePicker { tanggalJatuhTempo = it } }) {
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
                value = totalDenda,
                onValueChange = { input ->
                    val cleanInput = input.replace(".", "").filter { it.isDigit() }
                    totalDenda = if (cleanInput.isNotEmpty()) {
                        try {
                            NumberFormat.getInstance(Locale("in", "ID")).format(cleanInput.toLong())
                        } catch (e: Exception) {
                            Log.e("PerhitunganPiutangScreen", "Error formatting denda: ${e.message}")
                            ""
                        }
                    } else {
                        ""
                    }
                    Log.d("PerhitunganPiutangScreen", "Denda setelah input: $totalDenda")
                },
                label = { Text("Denda Tetap (Rp)") },
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
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        Log.d("AuthDebug", "Status autentikasi sebelum simpan: ${currentUser?.uid ?: "Tidak ada pengguna"}")
                        if (currentUser == null) {
                            Toast.makeText(context, "Harap login terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (tanggalPinjam == "Pilih Tanggal" || tanggalJatuhTempo == "Pilih Tanggal Jatuh Tempo") {
                            Toast.makeText(context, "Harap pilih tanggal pinjaman dan jatuh tempo!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (namaPinjaman.isEmpty()) {
                            Toast.makeText(context, "Nama pemberi pinjaman tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val pinjamanValue = nominalPinjaman.replace(".", "").replace(",", "").toDoubleOrNull()
                        val dendaValue = totalDenda.replace(".", "").replace(",", "").toDoubleOrNull()
                        if (pinjamanValue == null) {
                            Toast.makeText(context, "Nominal piutang tidak valid!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (dendaValue == null) {
                            Toast.makeText(context, "Denda tetap tidak valid!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        Log.d("PerhitunganPiutangScreen", "Mengirim data ke Firestore: nama=$namaPinjaman, nominal=$pinjamanValue, denda=$dendaValue, tanggalPinjam=$tanggalPinjam, tanggalJatuhTempo=$tanggalJatuhTempo, catatan=$catatan")

                        hutangViewModel.hitungDanSimpanHutang(
                            type = "Piutang",
                            hutangType = HutangType.PERHITUNGAN,
                            namapinjaman = namaPinjaman,
                            nominalpinjaman = pinjamanValue,
                            bunga = dendaValue,
                            lamaPinjam = 0,
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
                                popupMessage = "Gagal menyimpan piutang! Periksa log untuk detail."
                                showPopup = true
                                Log.e("PerhitunganPiutangScreen", "Gagal menyimpan piutang, docId: $docId")
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }

    LaunchedEffect(navigateToPreview) {
        navigateToPreview?.let { docId ->
            showPopup = false
            delay(500)
            Log.d("NavigationDebug", "Navigasi ke piutang_perhitungan_preview/$docId")
            try {
                navController.navigate("piutang_perhitungan_preview/$docId")
                navigateToPreview = null
            } catch (e: Exception) {
                Log.e("NavigationError", "Gagal navigasi: ${e.message}", e)
            }
        }
    }
}