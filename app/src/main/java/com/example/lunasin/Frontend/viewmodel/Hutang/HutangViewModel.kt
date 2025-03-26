package com.example.lunasin.Frontend.viewmodel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Backend.model.Tempo
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

class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _hutangState = MutableStateFlow<Hutang?>(null)
    val hutangState: StateFlow<Hutang?> = _hutangState

    private val _hutangList = MutableStateFlow<List<Hutang>>(emptyList())
    val hutangList: StateFlow<List<Hutang>> = _hutangList

    // Hitung denda
    fun dendaBunga_Tahunan(sisahutang:Double, bunga:Double, telat:Int): Double
    {
       val  Per_bunga = bunga / 100
       val denda1 = Per_bunga/365
        val denda2 = denda1 * sisahutang
       return denda2 * telat
    }

    fun dendaBunga_Bulan(sisahutang:Double, bunga:Double, telat:Int): Double
    {
        val  Per_bunga = bunga / 100
        val denda1 = Per_bunga/12
        val denda2 = denda1 * sisahutang
        return denda2 * telat
    }

    fun dendaTetap (denda : Int, Telat : Int) : Int
    {
        return denda * Telat
    }

    fun denda_Cicilan(sisahutang:Double, bunga:Double, telat:Int): Double
    {
        val  Per_bunga = bunga / 100
        val denda1 = Per_bunga * sisahutang
        return denda1 * telat
    }

    // Personaliasi Akun
    fun Debt_to_Income(Totalpembayaran: Double, Pendapatan: Double): String {
        val dti = (Totalpembayaran / Pendapatan) * 100
        return if (dti >= 40) {
            "Warning: DTI Anda $dti% - Risiko tinggi!"
        } else {
            "Aman: DTI Anda $dti% - Keuangan stabil."
        }
    }

    fun debt_Service_Coverage_Ratio(pendapatan: Double,totalcicilan: Double): String {
        val dscr = totalcicilan / pendapatan
        return if (dscr >= 1)
        {
            "Aman : Resiko Gagal bayar aman"
        }
        else
        {
            "Warning: Resiko Gagal bayar tinggi."
        }
    }

    fun rumus_Amorsiasi(angsuranPerbulan: Double, bunga: Double, lamaPinjam: Int): Double {
        val bunga1 = bunga/100
        val total1 = 1 + bunga1
        val total2_pembagi = pangkat(total1,lamaPinjam) - 1

        val total1_penyebut = pangkat(total1,lamaPinjam)
        val total2_penyebut = total1_penyebut * bunga1 * angsuranPerbulan

        return total2_penyebut/total2_pembagi
    }





//    fun getHutangById(docId: String) {
//        viewModelScope.launch {
//            try {
//                val document = firestore.collection("hutang").document(docId).get().await()
//                if (document.exists()) {
//                    val hutang = document.toObject(Hutang::class.java)
//                    _hutangState.value = hutang
//                    Log.d("FirestoreDebug", "Data hutang berhasil didapat: $hutang")
//                } else {
//                    Log.e("Firestore", "Dokumen tidak ditemukan!")
//                }
//            } catch (e: Exception) {
//                Log.e("Firestore", "Gagal mengambil data", e)
//            }
//        }
//    }
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



    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    fun pangkat(base: Double, exponent: Int): Double {
        var result = 1.0
        for (i in 1..exponent) {
            result *= base
        }
        return result
    }

    fun cicilanPerbulan(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaDesimal = bunga / 100.0
        val bungaR = 1 + bungaDesimal

        val total1 = nominalPinjaman * bungaDesimal * pangkat(bungaR, lamaPinjam)
        val total2 = pangkat(bungaR, lamaPinjam) - 1

        return if (total2 != 0.0) total1 / total2 else nominalPinjaman / lamaPinjam
    }

    fun hitungBunga(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaDesimal = bunga / 100.0
        return nominalPinjaman * bungaDesimal * lamaPinjam
    }

    fun hitungTanggalAkhir(tanggalPinjam: String, lamaPinjam: Int): String {
        return try {
            val possibleFormats = listOf("d/M/yyyy", "dd/MM/yyyy")

            val tanggalawal = possibleFormats.firstNotNullOfOrNull { format ->
                try {
                    LocalDate.parse(tanggalPinjam, DateTimeFormatter.ofPattern(format))
                } catch (e: Exception) {
                    null
                }
            } ?: throw IllegalArgumentException("Format tanggal tidak dikenali")

            val tanggalakhir = tanggalawal.plusMonths(lamaPinjam.toLong())
            tanggalakhir.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            Log.e("TanggalError", "Format tanggal salah: $tanggalPinjam", e)
            "Format Salah"
        }
    }

    fun hitungTotalHutang(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaTotal = hitungBunga(nominalPinjaman, bunga, lamaPinjam)
        return nominalPinjaman + bungaTotal
    }

    fun hitungDanSimpanHutang(
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjam: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val tanggalBayar = hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
        val totalHutang = hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
        val totalcicilan = cicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)
        val totalbunga = hitungBunga(nominalpinjaman, bunga, lamaPinjam)

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        calendar.time = sdf.parse(tanggalPinjam)!!

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

        val hutang = mapOf(
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "bunga" to bunga,
            "lamaPinjaman" to lamaPinjam,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalBayar" to tanggalBayar,
            "totalcicilan" to totalcicilan,
            "totalbunga" to totalbunga,
            "tanggalTempoDihitung" to true,
            "listTempo" to daftarTempo
        )

        Log.d("FirestoreDebug", "Mengirim data: $hutang")

        viewModelScope.launch {
            try {
                val docId = firestoreService.tambahHutang(hutang)
                if (docId != null) {
                    onResult(true, docId)
                    ambilDataHutang()
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Gagal mengirim data", e)
                onResult(false, null)
            }
        }
    }

    fun ambilDataHutang() {
        viewModelScope.launch {
            try {
                val result = firestoreService.getHutang()
                _hutangList.value = result ?: emptyList()
            } catch (e: Exception) {
                Log.e("FirestoreError", "Gagal mengambil daftar hutang", e)
                _hutangList.value = emptyList()
            }
        }
    }

    fun hapusHutang(documentId: String) {
        viewModelScope.launch {
            val result = firestoreService.hapusHutang(documentId)
            if (result) ambilDataHutang()
        }
    }
}
