package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Backend.Model.StatusBayar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    private val _hutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val hutangSayaList: StateFlow<List<Hutang>> = _hutangSayaList

    private val _hutangState = MutableStateFlow<Hutang?>(null)
    val hutangState: StateFlow<Hutang?> = _hutangState

    private val _recentSearch = MutableStateFlow<Hutang?>(null)
    val recentSearch: StateFlow<Hutang?> = _recentSearch

    private val _currentUserId = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
    val currentUserId: StateFlow<String> = _currentUserId

    private val firestore = FirebaseFirestore.getInstance()

    fun getHutangByIdTransaksi(idTransaksi: String) {
        viewModelScope.launch {
            try {
                Log.d("FirestoreDebug", "Mencari dokumen dengan Id_Transaksi: $idTransaksi")
                val querySnapshot = firestore.collection("hutang")
                    .whereEqualTo("Id_Transaksi", idTransaksi)
                    .get()
                    .await()
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    Log.d("FirestoreDebug", "Dokumen ditemukan: ${document.id}, data: ${document.data}")
                    var hutang = Hutang.fromMap(document.data ?: emptyMap()).copy(Id_Transaksi = idTransaksi, docId = document.id)
                    val tanggalSekarang = LocalDate.now()
                    hutang = when (hutang.hutangType) {
                        HutangType.SERIUS -> {
                            val dendaListTempo = HutangCalculator.hitungDendaListTempo(
                                listTempo = hutang.listTempo.map { it.toMap() },
                                nominalPerAngsuran = hutang.totalcicilan,
                                bunga = hutang.bunga,
                                tanggalSekarang = tanggalSekarang
                            )
                            hutang.copy(
                                totalDenda = dendaListTempo,
                                totalHutang = hutang.totalHutang + dendaListTempo
                            )
                        }
                        HutangType.PERHITUNGAN -> {
                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
                                tanggalSekarang = tanggalSekarang
                            )
                            val denda = if (keterlambatan > 0) {
                                hutang.totalDenda
                            } else {
                                0.0
                            }
                            hutang.copy(
                                totalHutang = hutang.nominalpinjaman + denda
                            )
                        }
                        HutangType.TEMAN -> {
                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
                                tanggalSekarang = tanggalSekarang
                            )
                            val denda = if (keterlambatan > 0) {
                                HutangCalculator.dendaBunga_Bulan(
                                    sisahutang = hutang.nominalpinjaman,
                                    bunga = 5.0,
                                    telat = keterlambatan / 30
                                )
                            } else {
                                0.0
                            }
                            hutang.copy(
                                totalDenda = denda,
                                totalHutang = hutang.nominalpinjaman + denda
                            )
                        }
                        null -> {
                            Log.e("HutangViewModel", "HutangType null untuk Id_Transaksi: $idTransaksi")
                            hutang
                        }
                    }
                    _hutangState.value = hutang
                    _recentSearch.value = hutang
                    Log.d("FirestoreDebug", "Data hutang berhasil didapat: $hutang")
                } else {
                    _hutangState.value = null
                    _recentSearch.value = null
                    Log.e("Firestore", "Dokumen tidak ditemukan untuk Id_Transaksi: $idTransaksi")
                }
            } catch (e: Exception) {
                _hutangState.value = null
                _recentSearch.value = null
                Log.e("Firestore", "Gagal mengambil data: ${e.message}", e)
            }
        }
    }

    fun getHutangById(docId: String) {
        viewModelScope.launch {
            try {
                Log.d("FirestoreDebug", "Mencari dokumen dengan docId: $docId")
                val document = firestore.collection("hutang").document(docId).get().await()
                if (document.exists()) {
                    var hutang = Hutang.fromMap(document.data ?: emptyMap()).copy(docId = document.id)
                    val tanggalSekarang = LocalDate.now()
                    hutang = when (hutang.hutangType) {
                        HutangType.SERIUS -> {
                            val dendaListTempo = HutangCalculator.hitungDendaListTempo(
                                listTempo = hutang.listTempo.map { it.toMap() },
                                nominalPerAngsuran = hutang.totalcicilan,
                                bunga = hutang.bunga,
                                tanggalSekarang = tanggalSekarang
                            )
                            hutang.copy(
                                totalDenda = dendaListTempo,
                                totalHutang = hutang.totalHutang + dendaListTempo
                            )
                        }
                        HutangType.PERHITUNGAN -> {
                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
                                tanggalSekarang = tanggalSekarang
                            )
                            val denda = if (keterlambatan > 0) {
                                hutang.totalDenda
                            } else {
                                0.0
                            }
                            hutang.copy(
                                totalHutang = hutang.nominalpinjaman + denda
                            )
                        }
                        HutangType.TEMAN -> {
                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
                                tanggalSekarang = tanggalSekarang
                            )
                            val denda = if (keterlambatan > 0) {
                                HutangCalculator.dendaBunga_Bulan(
                                    sisahutang = hutang.nominalpinjaman,
                                    bunga = 5.0,
                                    telat = keterlambatan / 30
                                )
                            } else {
                                0.0
                            }
                            hutang.copy(
                                totalDenda = denda,
                                totalHutang = hutang.nominalpinjaman + denda
                            )
                        }
                        null -> {
                            Log.e("HutangViewModel", "HutangType null untuk docId: $docId")
                            hutang
                        }
                    }
                    _hutangState.value = hutang
                    _recentSearch.value = hutang
                    Log.d("FirestoreDebug", "Data hutang berhasil didapat: $hutang")
                } else {
                    _hutangState.value = null
                    _recentSearch.value = null
                    Log.e("Firestore", "Dokumen tidak ditemukan untuk docId: $docId")
                }
            } catch (e: Exception) {
                _hutangState.value = null
                _recentSearch.value = null
                Log.e("Firestore", "Gagal mengambil data: ${e.message}", e)
            }
        }
    }

    fun clearRecentSearch() {
        _recentSearch.value = null
        Log.d("HutangViewModel", "Recent search telah dihapus")
    }

    fun clearHutangState() {
        _hutangState.value = null
        Log.d("HutangViewModel", "Hasil pencarian telah dihapus")
    }


    fun klaimHutang(idHutang: String, idPenerima: String) {
        val hutangRef = firestore.collection("hutang").document(idHutang)
        hutangRef.update("id_penerima", idPenerima)
            .addOnSuccessListener {
                Log.d("KlaimHutang", "Hutang berhasil diklaim oleh $idPenerima")
            }
            .addOnFailureListener {
                Log.e("KlaimHutang", "Gagal klaim hutang", it)
            }
    }

    fun ambilHutangSaya(userId: String) {
        viewModelScope.launch {
            try {
                val result = firestore.collection("hutang")
                    .whereEqualTo("id_penerima", userId)
                    .get()
                    .await()
                val tanggalSekarang = LocalDate.now()
                val daftarHutang = result.documents.mapNotNull { doc ->
                    var hutang = Hutang.fromMap(doc.data ?: emptyMap()).copy(Id_Transaksi = doc.id)
                    hutang = when (hutang.hutangType) {
                        HutangType.SERIUS -> {
                            val dendaListTempo = HutangCalculator.hitungDendaListTempo(
                                listTempo = hutang.listTempo.map { it.toMap() }, // Konversi List<Tempo> ke List<Map<String, Any>>
                                nominalPerAngsuran = hutang.totalcicilan,
                                bunga = hutang.bunga,
                                tanggalSekarang = tanggalSekarang
                            )
                            hutang.copy(
                                totalDenda = dendaListTempo,
                                totalHutang = hutang.totalHutang + dendaListTempo
                            )
                        }
                        HutangType.PERHITUNGAN -> {
                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
                                tanggalSekarang = tanggalSekarang
                            )
                            val denda = if (keterlambatan > 0) {
                                hutang.totalDenda
                            } else {
                                0.0
                            }
                            hutang.copy(
                                totalHutang = hutang.nominalpinjaman + denda
                            )
                        }
                        HutangType.TEMAN -> {
                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
                                tanggalSekarang = tanggalSekarang
                            )
                            val denda = if (keterlambatan > 0) {
                                HutangCalculator.dendaBunga_Bulan(
                                    sisahutang = hutang.nominalpinjaman,
                                    bunga = 5.0, // Default bunga jika tidak ada
                                    telat = keterlambatan / 30 // Konversi hari ke bulan
                                )
                            } else {
                                0.0
                            }
                            hutang.copy(
                                totalDenda = denda,
                                totalHutang = hutang.nominalpinjaman + denda
                            )
                        }
                        null -> {
                            // Tangani kasus ketika hutangType null
                            Log.e("HutangViewModel", "HutangType null untuk docId: ${doc.id}")
                            hutang
                        }
                    }
                    hutang
                }
                _hutangSayaList.value = daftarHutang
                Log.d("HutangViewModel", "Daftar hutang setelah hitung: $daftarHutang")
            } catch (e: Exception) {
                Log.e("HutangViewModel", "Gagal ambil hutang", e)
            }
        }
    }

    fun hitungDanSimpanHutang(
        type: String,
        hutangType: HutangType,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjam: String,
        tanggalJatuhTempo: String? = null,
        catatan: String,
        dendaTetap: Double? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("FirestoreError", "User belum login, tidak bisa menyimpan hutang")
            onResult(false, null)
            return
        }

        val userId = user.uid
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val parsedDate = sdf.parse(tanggalPinjam)
        if (parsedDate == null) {
            Log.e("FirestoreError", "Gagal mengonversi tanggal")
            onResult(false, null)
            return
        }
        calendar.time = parsedDate

        // Validasi tanggalJatuhTempo untuk tipe TEMAN dan PERHITUNGAN
        val effectiveTanggalJatuhTempo = when (hutangType) {
            HutangType.SERIUS -> {
                // Untuk SERIUS, hitung otomatis berdasarkan lamaPinjam
                HutangCalculator.hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
            }
            HutangType.TEMAN, HutangType.PERHITUNGAN -> {
                // Untuk TEMAN dan PERHITUNGAN, tanggalJatuhTempo wajib
                if (tanggalJatuhTempo.isNullOrEmpty()) {
                    Log.e("FirestoreError", "Tanggal jatuh tempo wajib untuk tipe $hutangType")
                    onResult(false, null)
                    return
                }
                tanggalJatuhTempo
            }
        }
        val idTransaksi = generateIdHutang()

        val hutangData = when (hutangType) {
            HutangType.SERIUS -> createHutangSeriusData(
                idTransaksi,userId, namapinjaman, nominalpinjaman, bunga, lamaPinjam, tanggalPinjam, catatan, calendar, sdf
            )
            HutangType.TEMAN -> createHutangTemanData(
                idTransaksi,userId, namapinjaman, nominalpinjaman, tanggalPinjam, effectiveTanggalJatuhTempo, catatan
            )
            HutangType.PERHITUNGAN -> createHutangPerhitunganData(
                idTransaksi,userId, namapinjaman, nominalpinjaman, dendaTetap ?: 0.0, tanggalPinjam, effectiveTanggalJatuhTempo, catatan
            )
        }

        val finalHutangData = hutangData.toMutableMap().apply {
            put("type", type)
        }

        firestore.collection("hutang").add(finalHutangData)
            .addOnSuccessListener { documentReference ->
                val docId = documentReference.id
                documentReference.update("docId", docId)
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Hutang berhasil disimpan dengan docId: $docId")
                        onResult(true, docId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Gagal menyimpan docId", e)
                        onResult(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Gagal menyimpan data hutang", e)
                onResult(false, null)
            }
    }

    private fun createHutangSeriusData(
        idTransaksi : String,
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjam: String,
        catatan: String,
        calendar: Calendar,
        sdf: SimpleDateFormat
    ): Map<String, Any?> {
        val tanggalJatuhTempo = HutangCalculator.hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
        val totalHutang = HutangCalculator.hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
        val listTempo = createTempoList(lamaPinjam, calendar, sdf)
        val totalCicilan = HutangCalculator.cicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)

        return mapOf(
            "Id_Transaksi" to idTransaksi,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "bunga" to bunga,
            "lamaPinjaman" to lamaPinjam,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalJatuhTempo" to tanggalJatuhTempo,
            "listTempo" to listTempo,
            "catatan" to catatan,
            "totalcicilan" to totalCicilan,
            "id_penerima" to "",
            "hutangType" to HutangType.SERIUS.name,
            "statusBayar" to StatusBayar.BELUM_LUNAS.name
        )
    }

    private fun createHutangTemanData(
        idTransaksi : String,
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        tanggalPinjam: String,
        tanggalJatuhTempo: String,
        catatan: String
    ): Map<String, Any?> {
        val totalHutang = nominalpinjaman
        return mapOf(
            "Id_Transaksi" to idTransaksi,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalJatuhTempo" to tanggalJatuhTempo,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.TEMAN.name,
            "statusBayar" to StatusBayar.BELUM_LUNAS.name
        )
    }

    private fun createHutangPerhitunganData(
        idTransaksi : String,
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        dendaTetap: Double,
        tanggalPinjam: String,
        tanggalJatuhTempo: String,
        catatan: String
    ): Map<String, Any?> {
        val denda = HutangCalculator.dendaTetap(dendaTetap)
        val totalHutang = nominalpinjaman // Denda tidak ditambahkan saat menyimpan
        return mapOf(
            "Id_Transaksi" to idTransaksi,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "totalDenda" to denda,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalJatuhTempo" to tanggalJatuhTempo,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.PERHITUNGAN.name,
            "statusBayar" to StatusBayar.BELUM_LUNAS.name
        )
    }

    private fun createTempoList(
        lamaPinjam: Int,
        calendar: Calendar,
        sdf: SimpleDateFormat
    ): List<Map<String, Any>> {
        val listTempo = mutableListOf<Map<String, Any>>()
        for (i in 1..lamaPinjam) {
            calendar.add(Calendar.MONTH, 1)
            listTempo.add(
                mapOf(
                    "angsuranKe" to i,
                    "tanggalTempo" to sdf.format(calendar.time)
                )
            )
        }
        return listTempo
    }

    private fun generateIdHutang(): String {
            val timestamp = SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(Date())
            val randomString = (1..5)
                .map { ('A'..'Z') + ('0'..'9') }
                .flatten()
                .shuffled()
                .take(5)
                .joinToString("")
        return "${timestamp}_$randomString"
    }
}