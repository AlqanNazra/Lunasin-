package com.example.lunasin.Frontend.UI.Hutang.Piutang

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.UI.Hutang.Hutang.InfoRow
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.theme.Black
import com.example.lunasin.utils.QrCodeDialogButton
import com.example.lunasin.utils.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewPiutangSeriusScreen(
    viewModel: HutangViewModel,
    navController: NavController,
    docId: String
) {
    Log.d("PREVIEW_PIUTANG_SERIUS_SCREEN", "docId: $docId")

    // Ambil data piutang berdasarkan docId
    LaunchedEffect(docId) {
        if (docId.isNotEmpty()) {
            viewModel.getHutangById(docId)
        }
    }

    val hutang by viewModel.hutangState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    actionColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Piutang Serius",
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
            if (hutang == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                // Bagian Informasi Utama
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Informasi Piutang",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        InfoRow(label = "Nama Penerima Pinjaman", value = hutang?.namapinjaman ?: "Data Kosong")
                        InfoRow(
                            label = "Nominal Piutang",
                            value = hutang?.nominalpinjaman?.let { formatRupiah(it) } ?: "Rp0,00"
                        )
                        InfoRow(
                            label = "Bunga",
                            value = "${hutang?.bunga ?: 0.0}%"
                        )
                        InfoRow(
                            label = "Periode",
                            value = "${hutang?.lamaPinjaman ?: 0} Bulan"
                        )
                        InfoRow(
                            label = "Tanggal",
                            value = "${hutang?.tanggalPinjam ?: "-"} hingga ${hutang?.tanggalBayar ?: "-"}"
                        )
                        InfoRow(
                            label = "Total Piutang",
                            value = hutang?.totalHutang?.let { formatRupiah(it) } ?: "Rp0,00"
                        )
                        InfoRow(
                            label = "Cicilan per Bulan",
                            value = hutang?.totalcicilan?.let { formatRupiah(it) } ?: "Rp0,00"
                        )
                    }
                }

                // Bagian Catatan
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Catatan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = hutang?.catatan ?: "Tidak ada catatan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Black.copy(alpha = 0.7f)
                        )
                    }
                }

                // Bagian Tombol Aksi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tombol Kembali
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

                    // Tombol Confirm
                    Button(
                        onClick = { navController.navigate("list_hutang_screen") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Confirm", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Tombol Lihat Jatuh Tempo (khusus untuk Piutang Serius)
                Button(
                    onClick = {
                        if (docId.isNotEmpty()) {
                            navController.navigate("hutang_serius_tempo/$docId")
                        } else {
                            Log.e("LihatJatuhTempo", "docId NULL atau kosong")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Lihat Jatuh Tempo", style = MaterialTheme.typography.labelLarge)
                }

                // QR Code Button
                hutang?.let { data ->
                    if (data.docId != null) {
                        QrCodeDialogButton(data = "lunasin://previewHutang?docId=${data.docId}")
                    }
                }
            }
        }
    }
}