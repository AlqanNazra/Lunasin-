package com.example.lunasin.Frontend.UI.Inputhutang

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lunasin.R

@Composable
fun PilihHutangScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Utang") },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 0.dp
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masukkan Pilihan Hutang",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF009688) // Warna hijau
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pilihan Hutang
            PilihanHutangItem(
                title = "Hutang Teman",
                description = "Hutang Biasa",
                backgroundColor = Color(0xFF00C853), // Hijau terang
                iconRes = R.drawable.ic_friend, // Ganti dengan ikon yang sesuai
                onClick = { navController.navigate("teman_hutang_screen") }
            )

            PilihanHutangItem(
                title = "Hutang Perhitungan",
                description = "Hutang dengan tambahan denda telat bayar",
                backgroundColor = Color(0xFFFF5252), // Merah terang
                iconRes = R.drawable.ic_calculator, // Ganti dengan ikon yang sesuai
                onClick = { navController.navigate("perhitungan_hutang_screen") }
            )

            PilihanHutangItem(
                title = "Hutang Serius",
                description = "Hutang dengan tambahan Bunga dan periode",
                backgroundColor = Color(0xFF2979FF), // Biru terang
                iconRes = R.drawable.ic_business, // Ganti dengan ikon yang sesuai
                onClick = { navController.navigate("input_hutang_screen") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Kembali
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kembali", color = Color.White)
            }
        }
    }
}

@Composable
fun PilihanHutangItem(
    title: String,
    description: String,
    backgroundColor: Color,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = description, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}
