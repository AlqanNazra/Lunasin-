package com.example.lunasin.Frontend.UI.Hutang

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.utils.CalendarView


@Composable
fun TanggalTempoScreen(viewModel: HutangViewModel, navController: NavController, docId: String) {
    val hutangState by viewModel.hutangState.collectAsState()

    LaunchedEffect(docId) {
        viewModel.getHutangById(docId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Tanggal Jatuh Tempo", style = MaterialTheme.typography.h6)

        val jatuhTempoList = hutangState?.listTempo?.map { it.tanggalTempo } ?: emptyList()
        CalendarView(jatuhTempoList)

        hutangState?.let { hutang ->
            val listTempo = hutang.listTempo ?: emptyList()

            LazyColumn(
                modifier = Modifier.weight(1f) // 🚀 Tambahkan weight agar tombol tidak tertutup
            ) {
                items(listTempo) { tempo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = 4.dp
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pembayaran ${tempo.angsuranKe}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "Tanggal Tempo: ${tempo.tanggalTempo}")
                            }
                        }
                    }
                }
            }
        } ?: Text(text = "Tidak ada data hutang.", modifier = Modifier.padding(top = 8.dp))


        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Kembali", color = Color.White)
        }
    }
}
