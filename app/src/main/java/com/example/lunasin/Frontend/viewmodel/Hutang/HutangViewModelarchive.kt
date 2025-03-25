//package com.example.lunasin.Frontend.viewmodel.Hutang
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.lunasin.Backend.Service.management_BE.FirestoreService
//import com.example.lunasin.Backend.model.Hutang
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import java.text.NumberFormat
//import java.text.SimpleDateFormat
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import java.util.Calendar
//import java.util.Locale
//
//class HutangViewModelarchive(private val firestoreService: FirestoreService) : ViewModel() {
//    private val firestore = FirebaseFirestore.getInstance()
//    private val _hutangState = MutableStateFlow<Hutang?>(null)
//    val hutangState: StateFlow<Hutang?> = _hutangState
//
//    private val _hutangList = MutableStateFlow<List<Hutang>>(emptyList())
//    val hutangList: StateFlow<List<Hutang>> = _hutangList
//
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
//
//    fun formatRupiah(amount: Double): String {
//        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
//        return formatter.format(amount)
//    }
//
//    fun pangkat(base: Double, exponent: Int): Double {
//        var result = 1.0
//        for (i in 1..exponent) {
//            result *= base
//        }
//        return result
//    }
//
//    fun cicilanPerbulan(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
//        val bungaDesimal = bunga / 100.0
//        val bungaR = 1 + bungaDesimal
//
//        val total1 = nominalPinjaman * bungaDesimal * pangkat(bungaR, lamaPinjam)
//        val total2 = pangkat(bungaR, lamaPinjam) - 1
//
//        return if (total2 != 0.0) total1 / total2 else nominalPinjaman / lamaPinjam  // Hindari pembagian dengan nol
//    }
//
//    fun hitungBunga(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
//        val bungaDesimal = bunga / 100.0
//        return nominalPinjaman * bungaDesimal * lamaPinjam
//    }
//
//    fun hitungTanggalAkhir(tanggalPinjam: String, lamaPinjam: Int): String {
//        return try {
//            val possibleFormats = listOf("d/M/yyyy", "dd/MM/yyyy")
//
//            val tanggalawal = possibleFormats.firstNotNullOfOrNull { format ->
//                try {
//                    LocalDate.parse(tanggalPinjam, DateTimeFormatter.ofPattern(format))
//                } catch (e: Exception) {
//                    null
//                }
//            } ?: throw IllegalArgumentException("Format tanggal tidak dikenali")
//
//            val tanggalakhir = tanggalawal.plusMonths(lamaPinjam.toLong())
//            tanggalakhir.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//        } catch (e: Exception) {
//            Log.e("TanggalError", "Format tanggal salah: $tanggalPinjam", e)
//            "Format Salah"
//        }
//    }
//
//    fun hitungTotalHutang(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
//        val bungaTotal = hitungBunga(nominalPinjaman, bunga, lamaPinjam)
//        return nominalPinjaman + bungaTotal
//    }
//
//    fun hitungDanSimpanHutang(
//        namapinjaman: String,
//        nominalpinjaman: Double,
//        bunga: Double,
//        lamaPinjam: Int,
//        tanggalPinjam: String,
//        onResult: (Boolean, String?) -> Unit
//    ) {
//        val tanggalBayar = hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
//        val totalHutang = hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
//        val totalcicilan = cicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)
//        val totalbunga = hitungBunga(nominalpinjaman, bunga, lamaPinjam)
//
//        val hutang = mapOf(
//            "namapinjaman" to namapinjaman,
//            "nominalpinjaman" to nominalpinjaman,
//            "bunga" to bunga,
//            "lamaPinjaman" to lamaPinjam,
//            "totalHutang" to totalHutang,
//            "tanggalPinjam" to tanggalPinjam,
//            "tanggalBayar" to tanggalBayar,
//            "totalcicilan" to totalcicilan,
//            "totalbunga" to totalbunga
//        )
//
//        Log.d("FirestoreDebug", "Mengirim data: $hutang")
//
//        viewModelScope.launch {
//            try {
//                val docId = firestoreService.tambahHutang(hutang)
//                Log.d("FirestoreDebug", "docId: $docId")
//
//                if (docId != null) {
//                    onResult(true, docId)
//                    ambilDataHutang()
//                } else {
//                    onResult(false, null)
//                }
//            } catch (e: Exception) {
//                Log.e("FirestoreError", "Gagal mengirim data", e)
//                onResult(false, null)
//            }
//        }
//    }
//
//    fun hitungDanSimpanTanggalTempo(hutangId: String, tanggalPinjam: String, lamaPinjam: Int) {
//        val db = FirebaseFirestore.getInstance()
//        val tempoRef = db.collection("tanggal_tempo").document(hutangId)
//
//        val tanggalAwal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(tanggalPinjam)
//        val calendar = Calendar.getInstance()
//        calendar.time = tanggalAwal!!
//
//        val daftarTempo = mutableListOf<Map<String, Any>>()
//
//        for (i in 1..lamaPinjam) {
//            calendar.add(Calendar.MONTH, 1)
//            val tanggalTempo = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
//
//            daftarTempo.add(
//                mapOf(
//                    "angsuranKe" to i,
//                    "tanggalTempo" to tanggalTempo
//                )
//            )
//        }
//
//        tempoRef.set(mapOf("listTempo" to daftarTempo))
//            .addOnSuccessListener {
//                Log.d("HutangViewModel", "Tanggal Tempo berhasil disimpan!")
//
//                // 🔥 Update status "tanggalTempoDihitung" jadi true di Firestore
//                db.collection("hutang").document(hutangId)
//                    .update("tanggalTempoDihitung", true)
//            }
//            .addOnFailureListener {
//                Log.e("HutangViewModel", "Gagal menyimpan tanggal tempo!", it)
//            }
//    }
//
//    suspend fun tambahHutang(hutang: Map<String, Any>): String? {
//        return try {
//            val document = firestore.collection("hutang").add(hutang).await()
//            document.id
//        } catch (e: Exception) {
//            Log.e("FirestoreError", "Gagal menyimpan data ke Firestore", e)
//            null
//        }
//    }
//
//    fun ambilDataHutang() {
//        viewModelScope.launch {
//            try {
//                val result = firestoreService.getHutang()
//                _hutangList.value = result ?: emptyList() // 🔥 Hindari nilai `null`
//            } catch (e: Exception) {
//                Log.e("FirestoreError", "Gagal mengambil daftar hutang", e)
//                _hutangList.value = emptyList() // 🔥 Berikan nilai default jika gagal
//            }
//        }
//    }
//
//
//    fun hapusHutang(documentId: String) {
//        viewModelScope.launch {
//            val result = firestoreService.hapusHutang(documentId)
//            if (result) ambilDataHutang()
//        }
//    }
//
//}
