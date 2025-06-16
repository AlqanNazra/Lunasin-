package com.example.lunasin.Frontend.UI.Inputhutang

// DIUBAH KE M3: Mengganti semua import ke material3
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.R

@OptIn(ExperimentalMaterial3Api::class) // DIUBAH KE M3
@Composable
fun PilihHutangScreen(navController: NavController) {
    // DIUBAH KE M3: Menggunakan Scaffold M3
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Utang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Menambahkan scroll
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pilih Jenis Pencatatan Utang",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary // DIUBAH KE M3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pilihan Hutang
            PilihanHutangItem(
                title = "Utang Biasa",
                description = "Mencatat utang tanpa bunga atau denda.",
                // DIUBAH KE M3: Menggunakan warna dari tema
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconRes = R.drawable.ic_friend,
                onClick = { navController.navigate("teman_hutang_screen") }
            )

            PilihanHutangItem(
                title = "Utang dengan Denda",
                description = "Mencatat utang dengan tambahan denda jika telat.",
                // DIUBAH KE M3: Menggunakan warna error dari tema
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                iconRes = R.drawable.ic_calculator,
                onClick = { navController.navigate("perhitungan_hutang_screen") }
            )

            PilihanHutangItem(
                title = "Utang dengan Bunga",
                description = "Mencatat utang dengan bunga dan periode.",
                // DIUBAH KE M3: Menggunakan warna secondary dari tema
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconRes = R.drawable.ic_business,
                onClick = { navController.navigate("input_hutang_screen") }
            )

            Spacer(modifier = Modifier.weight(1f)) // Spacer untuk mendorong tombol ke bawah

            // Tombol Kembali
            Button(
                onClick = { navController.popBackStack() },
                // DIUBAH KE M3
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kembali", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun PilihanHutangItem(
    title: String,
    description: String,
    containerColor: Color, // DIUBAH KE M3: Menerima warna container
    contentColor: Color,   // DIUBAH KE M3: Menerima warna konten (teks/ikon)
    iconRes: Int,
    onClick: () -> Unit
) {
    // DIUBAH KE M3: Menggunakan Card M3
    Card(
        shape = RoundedCornerShape(16.dp), // Membuat sudut lebih bulat
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor // Mengatur warna latar belakang Card
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = contentColor, // Menggunakan warna konten yang sesuai
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
                Text(text = description, fontSize = 14.sp, color = contentColor, lineHeight = 18.sp)
            }
        }
    }
}