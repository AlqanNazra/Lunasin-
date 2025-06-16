package com.example.lunasin.Frontend.UI.Inputhutang

// DIUBAH KE M3: Mengganti semua import ke material3
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // DIUBAH KE M3: Menggunakan warna dari tema
        Text("Masukkan Data Hutang", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Text("Lengkapi informasi di bawah untuk mencatat utang dengan bunga.", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // DIUBAH KE M3: Menggunakan komponen Material3
        OutlinedTextField(
            value = namaPinjaman,
            onValueChange = { namaPinjaman = it },
            label = { Text("Nama Pinjaman") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nominalPinjaman, // Disederhanakan: tidak perlu .toString()
            onValueChange = { if (it.all(Char::isDigit)) nominalPinjaman = it },
            label = { Text("Nominal Pinjaman") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = bunga, // Disederhanakan
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) bunga = it },
            label = { Text("Bunga per Bulan (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Diubah ke Decimal
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = periodePinjaman, // Disederhanakan
            onValueChange = { if (it.all(Char::isDigit)) periodePinjaman = it },
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
                val nominalValue = nominalPinjaman.toDoubleOrNull()
                val bungaValue = bunga.toDoubleOrNull()
                val periodeValue = periodePinjaman.toIntOrNull()

                if (tanggalPinjam == "Pilih Tanggal" || namaPinjaman.isBlank() || nominalValue == null || bungaValue == null || periodeValue == null) {
                    Toast.makeText(context, "Semua kolom wajib diisi dengan benar!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                Log.d("SeriusHutangScreen", "Mengirim data ke Firestore...")

                hutangViewModel.hitungDanSimpanHutang(
                    hutangType = HutangViewModel.HutangType.SERIUS,
                    namaPinjaman, nominalValue, bungaValue, periodeValue, tanggalPinjam, catatan
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
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary // DIUBAH KE M3
                )
            } else {
                Text("Confirm")
            }
        }

        LaunchedEffect(navigateToPreview) {
            navigateToPreview?.let { docId ->
                delay(500)
                navController.navigate("preview_hutang/$docId")
                navigateToPreview = null
            }
        }

        // DIUBAH KE M3: AlertDialog menggunakan sintaks M3
        if (showPopup && !isLoading) { // Popup tidak ditampilkan saat loading
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