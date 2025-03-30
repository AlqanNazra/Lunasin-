package com.example.lunasin.Frontend.viewmodel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Backend.model.Tempo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import com.example.lunasin.Frontend.viewmodel.Hutang.HitungHutangaViewModel


class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _hutangState = MutableStateFlow<Hutang?>(null)
    val hutangState: StateFlow<Hutang?> = _hutangState

    private val _hutangList = MutableStateFlow<List<Hutang>>(emptyList())
    val hutangList: StateFlow<List<Hutang>> = _hutangList

fun getHutangById(docId: String) {
    viewModelScope.launch {
        try {
            val document = firestore.collection("hutang").document(docId).get().await()
            if (document.exists()) {
                val hutang = document.toObject(Hutang::class.java)

                val tempoList = (document["listTempo"] as? List<Map<String, Any>>)?.map { tempo ->
                    Tempo(
                        angsuranKe = (tempo["angsuranKe"] as? Long)?.toInt() ?: 0,
                        tanggalTempo = tempo["tanggalTempo"] as? String ?: ""
                    )
                } ?: emptyList()

                val hutangDenganTempo = hutang?.copy(listTempo = tempoList)

                _hutangState.value = hutangDenganTempo
                Log.d("FirestoreDebug", "Data hutang berhasil didapat: $hutangDenganTempo")
            } else {
                Log.e("Firestore", "Dokumen tidak ditemukan!")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Gagal mengambil data", e)
        }
    }
}

    fun hitungDanSimpanHutang(
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjam: String,
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val tanggalBayar = hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
        val totalHutang = hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
        val totalcicilan = cicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)
        val totalbunga = hitungBunga(nominalpinjaman, bunga, lamaPinjam)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val parsedDate = sdf.parse(tanggalPinjam)
        if (parsedDate == null) {
            Log.e("FirestoreError", "Gagal mengonversi tanggal")
            onResult(false, null)
            return
        }
        calendar.time = parsedDate

        val daftarTempo = mutableListOf<Map<String, Any>>()
        for (i in 1..lamaPinjam) {
            calendar.add(Calendar.MONTH, 1)
            daftarTempo.add(
                mapOf(
                    "angsuranKe" to i,
                    "tanggalTempo" to sdf.format(calendar.time)
                )
            )
        }

        val hutangData = mapOf(
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "bunga" to bunga,
            "lamaPinjaman" to lamaPinjam,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalBayar" to tanggalBayar,
            "totalcicilan" to totalcicilan,
            "totalbunga" to totalbunga,
            "listTempo" to daftarTempo
        )

        Log.d("FirestoreDebug", "Mengirim data: $hutangData")

        viewModelScope.launch {
            try {
                val docId = firestoreService.tambahHutang(hutangData)
                if (docId != null) {
                    onResult(true, docId)
                    ambilDataHutang(userId) // Ambil data sesuai userId setelah menyimpan
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Gagal mengirim data", e)
                onResult(false, null)
            }
        }
    }

    fun ambilDataHutang(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("HutangViewModel", "Mengambil data hutang untuk userId: $userId")
                val result = firestoreService.getHutang()
                    ?.filter { it.userId == userId } // Filter data sesuai userId

                if (result.isNullOrEmpty()) {
                    Log.e("HutangViewModel", "Data hutang kosong untuk userId: $userId")
                } else {
                    Log.d("HutangViewModel", "Data hutang ditemukan: ${result.size} item")
                }

                _hutangList.value = result ?: emptyList()
            } catch (e: Exception) {
                Log.e("FirestoreError", "Gagal mengambil daftar hutang", e)
                _hutangList.value = emptyList()
            }
        }
    }

    fun tambahHutang(hutang: Hutang) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val hutangDenganUserId = hutang.copy(userId = user.uid) // Set userId sebelum disimpan
                firestoreService.simpanHutang(hutangDenganUserId)
                Log.d("HutangViewModel", "Hutang berhasil disimpan dengan userId: ${user.uid}")
            } else {
                Log.e("HutangViewModel", "Gagal menyimpan hutang: User tidak ditemukan")
            }
        }
    }


    fun hapusHutang(documentId: String) {
        viewModelScope.launch {
            val result = firestoreService.hapusHutang(documentId)
            if (result) ambilDataHutang(userId = "")
        }
    }
}
