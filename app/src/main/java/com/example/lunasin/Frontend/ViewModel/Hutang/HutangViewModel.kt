package com.example.lunasin.Frontend.ViewModel.Hutang

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Backend.Model.StatusBayar
import com.example.lunasin.utils.NotifikasiUtils
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

//    fun getHutangByIdTransaksi(idTransaksi: String) {
//        viewModelScope.launch {
//            try {
//                Log.d("FirestoreDebug", "Mencari dokumen dengan Id_Transaksi: $idTransaksi")
//                val querySnapshot = firestore.collection("hutang")
//                    .whereEqualTo("Id_Transaksi", idTransaksi)
//                    .get()
//                    .await()
//                if (!querySnapshot.isEmpty) {
//                    val document = querySnapshot.documents.first()
//                    Log.d("FirestoreDebug", "Dokumen ditemukan: ${document.id}, data: ${document.data}")
//                    var hutang = Hutang.fromMap(document.data ?: emptyMap()).copy(Id_Transaksi = idTransaksi, docId = document.id)
//                    val tanggalSekarang = LocalDate.now()
//                    hutang = when (hutang.hutangType) {
//                        HutangType.SERIUS -> {
//                            val dendaListTempo = HutangCalculator.hitungDendaListTempo(
//                                listTempo = hutang.listTempo.map { it.toMap() },
//                                nominalPerAngsuran = hutang.totalcicilan,
//                                bunga = hutang.bunga,
//                                tanggalSekarang = tanggalSekarang
//                            )
//                            hutang.copy(
//                                totalDenda = dendaListTempo,
//                                totalHutang = hutang.totalHutang + dendaListTempo
//                            )
//                        }
//                        HutangType.PERHITUNGAN -> {
//                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
//                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
//                                tanggalSekarang = tanggalSekarang
//                            )
//                            val denda = if (keterlambatan > 0) {
//                                hutang.totalDenda
//                            } else {
//                                0.0
//                            }
//                            hutang.copy(
//                                totalHutang = hutang.nominalpinjaman + denda
//                            )
//                        }
//                        HutangType.TEMAN -> {
//                            val keterlambatan = HutangCalculator.hitungKeterlambatan(
//                                tanggalJatuhTempo = hutang.tanggalJatuhTempo,
//                                tanggalSekarang = tanggalSekarang
//                            )
//                            val denda = if (keterlambatan > 0) {
//                                HutangCalculator.dendaBunga_Bulan(
//                                    sisahutang = hutang.nominalpinjaman,
//                                    bunga = 5.0,
//                                    telat = keterlambatan / 30
//                                )
//                            } else {
//                                0.0
//                            }
//                            hutang.copy(
//                                totalDenda = denda,
//                                totalHutang = hutang.nominalpinjaman + denda
//                            )
//                        }
//                        null -> {
//                            Log.e("HutangViewModel", "HutangType null untuk Id_Transaksi: $idTransaksi")
//                            hutang
//                        }
//                    }
//                    _hutangState.value = hutang
//                    _recentSearch.value = hutang
//                    Log.d("FirestoreDebug", "Data hutang berhasil didapat: $hutang")
//                } else {
//                    _hutangState.value = null
//                    _recentSearch.value = null
//                    Log.e("Firestore", "Dokumen tidak ditemukan untuk Id_Transaksi: $idTransaksi")
//                }
//            } catch (e: Exception) {
//                _hutangState.value = null
//                _recentSearch.value = null
//                Log.e("Firestore", "Gagal mengambil data: ${e.message}", e)
//            }
//        }
//    }

    fun getHutangById(docId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e("FirestoreError", "Pengguna tidak login")
                    onError("Harap login terlebih dahulu")
                    return@launch
                }
                Log.d("FirestoreDebug", "Mencari dokumen dengan docId: $docId untuk pengguna: ${currentUser.uid}")
                val document = firestore.collection("hutang").document(docId).get().await()
                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    Log.d("FirestoreDebug", "Dokumen ditemukan: ${document.id}, userId: ${data["userId"]}, id_penerima: ${data["id_penerima"]}")
                    var hutang = Hutang.fromMap(data).copy(docId = document.id)
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
                    Log.w("Firestore", "Dokumen tidak ditemukan untuk docId: $docId")
                    onError("Dokumen tidak ditemukan")
                }
            } catch (e: Exception) {
                _hutangState.value = null
                _recentSearch.value = null
                Log.e("FirestoreError", "Gagal mengambil data: ${e.message}", e)
                if (e.message?.contains("PERMISSION_DENIED") == true) {
                    Log.w("SecurityWarning", "Akses ditolak untuk docId: $docId, pengguna: ${FirebaseAuth.getInstance().currentUser?.uid}")
                    onError("Anda tidak memiliki izin untuk mengakses data ini")
                } else {
                    onError("Gagal mengambil data: ${e.message}")
                }
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

    fun klaimHutang(hutang: Hutang, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null || user.isAnonymous) {
                    Log.e("FirestoreError", "Pengguna tidak terautentikasi atau anonim")
                    onResult(false, "Harap login dengan akun yang valid")
                    return@launch
                }
                val userId = user.uid
                val hutangRef = firestore.collection("hutang").document(hutang.docId)

                // Periksa keberadaan dokumen
                val docSnapshot = hutangRef.get().await()
                if (!docSnapshot.exists()) {
                    Log.e("FirestoreError", "Dokumen tidak ditemukan: ${hutang.docId}")
                    onResult(false, "Hutang tidak ditemukan")
                    return@launch
                }

                Log.d("FirestoreDebug", "Mengklaim hutang: docId=${hutang.docId}, id_penerima=$userId")
                hutangRef.update(
                    mapOf(
                        "isClaimed" to true,
                        "claimedBy" to userId,
                        "id_penerima" to userId
                    )
                ).await()
                Log.d("FirestoreDebug", "Hutang berhasil diklaim oleh: $userId")
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("FirestoreError", "Gagal mengklaim hutang: ${e.message}", e)
                if (e.message?.contains("PERMISSION_DENIED") == true) {
                    onResult(false, "Anda tidak memiliki izin untuk mengklaim hutang ini")
                } else {
                    onResult(false, e.message)
                }
            }
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
                            Log.e("HutangViewModel", "HutangType null untuk docId: ${doc.id}")
                            hutang
                        }
                    }
                    hutang
                }
                _hutangSayaList.value = daftarHutang
                Log.d("HutangViewModel", "Daftar hutang setelah hitung: $daftarHutang")
            } catch (e: Exception) {
                Log.e("HutangViewModel", "Gagal ambil hutang: ${e.message}", e)
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
        Log.d("FirestoreDebug", "UID pengguna: $userId")

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Validasi tanggalPinjam
        val parsedDate = try {
            sdf.parse(tanggalPinjam)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Format tanggal pinjaman tidak valid: $tanggalPinjam", e)
            onResult(false, null)
            return
        }
        if (parsedDate == null) {
            Log.e("FirestoreError", "Gagal mengonversi tanggal pinjaman: $tanggalPinjam")
            onResult(false, null)
            return
        }

        // Validasi tanggalJatuhTempo untuk TEMAN dan PERHITUNGAN
        val effectiveTanggalJatuhTempo = when (hutangType) {
            HutangType.SERIUS -> {
                HutangCalculator.hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
            }
            HutangType.TEMAN, HutangType.PERHITUNGAN -> {
                if (tanggalJatuhTempo.isNullOrEmpty()) {
                    Log.e("FirestoreError", "Tanggal jatuh tempo wajib untuk tipe $hutangType")
                    onResult(false, null)
                    return
                }
                try {
                    sdf.parse(tanggalJatuhTempo)
                    tanggalJatuhTempo
                } catch (e: Exception) {
                    Log.e("FirestoreError", "Format tanggal jatuh tempo tidak valid: $tanggalJatuhTempo", e)
                    onResult(false, null)
                    return
                }
            }
        }

        val idTransaksi = generateIdHutang()
        val calendar = Calendar.getInstance().apply { time = parsedDate }

        val hutangData = when (hutangType) {
            HutangType.SERIUS -> createHutangSeriusData(
                idTransaksi, userId, namapinjaman, nominalpinjaman, bunga, lamaPinjam, tanggalPinjam, catatan, calendar, sdf
            )
            HutangType.TEMAN -> createHutangTemanData(
                idTransaksi, userId, namapinjaman, nominalpinjaman, tanggalPinjam, effectiveTanggalJatuhTempo, catatan
            )
            HutangType.PERHITUNGAN -> createHutangPerhitunganData(
                idTransaksi, userId, namapinjaman, nominalpinjaman, dendaTetap ?: 0.0, tanggalPinjam, effectiveTanggalJatuhTempo, catatan
            )
        }

        val finalHutangData = hutangData.toMutableMap().apply {
            put("type", type)
        }

        Log.d("FirestoreDebug", "Data yang akan disimpan: $finalHutangData")
        firestore.collection("hutang").add(finalHutangData)
            .addOnSuccessListener { documentReference ->
                val docId = documentReference.id
                Log.d("FirestoreDebug", "Dokumen berhasil disimpan dengan ID: $docId")
                documentReference.update("docId", docId)
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Hutang berhasil disimpan dengan docId: $docId")
                        onResult(true, docId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Gagal memperbarui docId untuk hutang: ${e.message}", e)
                        onResult(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Gagal menyimpan data hutang: ${e.message}", e)
                Log.d("FirestoreDebug", "Data yang gagal disimpan: $finalHutangData")
                onResult(false, null)
            }
    }

    private fun createHutangSeriusData(
        idTransaksi: String,
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
        idTransaksi: String,
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
        idTransaksi: String,
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