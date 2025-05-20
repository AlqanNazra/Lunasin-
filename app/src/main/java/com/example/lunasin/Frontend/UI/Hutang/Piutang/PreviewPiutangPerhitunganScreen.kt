package com.example.lunasin.Frontend.UI.Hutang.Hutang

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.utils.QrCodeDialogButton
import com.example.lunasin.utils.formatRupiah
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewPiutangPerhitunganScreen(
    viewModel: HutangViewModel,
    navController: NavController,
    docId: String
) {
    Log.d("PREVIEW_PIUTANG_PERHITUNGAN_SCREEN", "docId: $docId")

    LaunchedEffect(docId) {
        if (docId.isNotEmpty()) {
            viewModel.getHutangById(docId)
        }
    }

    val hutangState by viewModel.hutangState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showQrCodeDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    actionColor = MaterialTheme.colorScheme.onSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { /* Empty title */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Action share */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (hutangState?.docId != null) {
                        IconButton(
                            onClick = { showQrCodeDialog = true },
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Generate QR Code",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
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
        // Store hutangState in a local variable to avoid smart cast issue
        val hutang = hutangState

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detail Piutang Perhitungan",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            if (hutang == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                // Header Nominal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(color = MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Total Piutang",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = hutang.totalHutang?.let { formatRupiah(it) } ?: "Rp0,00",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Informasi Utama
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nama Penerima",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = hutang.namapinjaman ?: "Data Kosong",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "Tanggal Pinjam",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = hutang.tanggalPinjam ?: "-",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "Tanggal Jatuh Tempo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = hutang.tanggalJatuhTempo ?: "-",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "Status Pembayaran",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = hutang.statusBayar?.name?.replace("_", " ")?.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            } ?: "-",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "Denda Tetap",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = hutang.totalDenda?.let { formatRupiah(it) } ?: "Rp0,00",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Catatan
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
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
                            text = hutang.catatan ?: "Tidak ada catatan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Tombol Aksi
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
                    FilledTonalButton(
                        onClick = { navController.navigate("list_hutang_screen") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Confirm", style = MaterialTheme.typography.labelLarge)
                    }
                }
                FilledTonalButton(
                    onClick = {
                        if (docId.isNotEmpty()) {
                            navController.navigate("tanggalTempo/$docId")
                        } else {
                            Log.e("LihatJatuhTempo", "docId NULL atau kosong")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Lihat Jatuh Tempo", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        // Tampilkan QR Code Dialog jika showQrCodeDialog bernilai true
        if (showQrCodeDialog && hutang?.docId != null) {
            QrCodeDialogButton(
                data = "lunasin://previewHutang?docId=${hutang.docId}",
                onDismissRequest = { showQrCodeDialog = false }
            )
        }
    }
}