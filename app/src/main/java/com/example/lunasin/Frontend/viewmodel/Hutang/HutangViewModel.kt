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
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator.hitungTotalHutang


class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    enum class HutangType {
        SERIUS,
        TEMAN,
        PERHITUNGAN
    }
    private val _hutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val hutangSayaList: StateFlow<List<Hutang>> = _hutangSayaList

    private val _piutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val piutangSayaList: StateFlow<List<Hutang>> = _piutangSayaList


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

    fun ambilDataPiutang(userId: String) {
        viewModelScope.launch {
            firestore.collection("hutang")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    val daftarHutang = result.documents.mapNotNull { doc ->
                        doc.toObject(Hutang::class.java)?.copy(docId = doc.id) // Pastikan docId diambil
                    }
                    _hutangList.value = daftarHutang
                    Log.d("HutangViewModel", "Data piutang berhasil diambil: $daftarHutang")
                }
                .addOnFailureListener { e ->
                    Log.e("HutangViewModel", "Gagal mengambil data piutang", e)
                }
        }
    }

    fun ambilDataHutang(userId: String) {
        viewModelScope.launch {
            firestore.collection("hutang")
                .whereEqualTo("id_penerima", userId)
                .get()
                .addOnSuccessListener { result ->
                    val daftarHutang = result.documents.mapNotNull { doc ->
                        doc.toObject(Hutang::class.java)?.copy(docId = doc.id) // Pastikan docId diambil
                    }
                    _hutangList.value = daftarHutang
                    Log.d("HutangViewModel", "Data hutang berhasil diambil: $daftarHutang")
                }
                .addOnFailureListener { e ->
                    Log.e("HutangViewModel", "Gagal mengambil data hutang", e)
                }
        }
    }



    fun tambahHutang(hutang: Hutang) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val hutangDenganUserId = hutang.copy(userId = user.uid)
                firestoreService.simpanHutang(hutangDenganUserId) { sukses ->
                    if (sukses) {
                        Log.d("HutangViewModel", "Hutang berhasil disimpan dengan userId: ${user.uid}")
                    } else {
                        Log.e("HutangViewModel", "Gagal menyimpan hutang ke Firestore")
                    }
                }
            } else {
                Log.e("HutangViewModel", "Gagal menyimpan hutang: User tidak ditemukan")
            }
        }
    }

    fun hitungDanSimpanHutang(
        hutangType: HutangViewModel.HutangType,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjam: String,
        catatan: String,
        onResult: (Boolean, String?) -> Unit,
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

        val db = FirebaseFirestore.getInstance()
        val hutangCollection = db.collection("hutang")

        val hutangData: Map<String, Any> = when (hutangType) {
            HutangViewModel.HutangType.SERIUS -> {
                val tanggalBayar = HutangCalculator.hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
                val totalHutang = HutangCalculator.hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
                val listTempo = createTempoList(lamaPinjam, calendar, sdf)
                val totalcicilan = HutangCalculator.cicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)

                mapOf(
                    "userId" to userId,
                    "namapinjaman" to namapinjaman,
                    "nominalpinjaman" to nominalpinjaman,
                    "bunga" to bunga,
                    "lamaPinjaman" to lamaPinjam,
                    "totalHutang" to totalHutang,
                    "tanggalPinjam" to tanggalPinjam,
                    "tanggalBayar" to tanggalBayar,
                    "listTempo" to listTempo,
                    "catatan" to catatan,
                    "totalcicilan" to totalcicilan,
                )
            }

            HutangViewModel.HutangType.TEMAN -> {
                mapOf(
                    "userId" to userId,
                    "namapinjaman" to namapinjaman,
                    "nominalpinjaman" to nominalpinjaman,
                    "tanggalPinjam" to tanggalPinjam,
                    "catatan" to catatan,
                    "id_penerima" to ""
                )
            }

            HutangViewModel.HutangType.PERHITUNGAN -> {
                val totalHutang = HutangCalculator.dendaTetap(nominalpinjaman, bunga)
                val listTempo = createTempoList(lamaPinjam, calendar, sdf)

                mapOf(
                    "userId" to userId,
                    "namapinjaman" to namapinjaman,
                    "nominalpinjaman" to nominalpinjaman,
                    "totalHutang" to totalHutang,
                    "tanggalPinjam" to tanggalPinjam,
                    "listTempo" to listTempo,
                    "catatan" to catatan
                )
            }
        }
        hutangCollection.add(hutangData)
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




    private fun createTempoList(lamaPinjam: Int, calendar: Calendar, sdf: SimpleDateFormat): List<Map<String, Any>> {
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

    fun hapusHutang(documentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = firestoreService.hapusHutang(documentId)
            if (result) {
                Log.d("HapusHutang", "Hutang dengan docId $documentId berhasil dihapus.")
                onSuccess() // Panggil ulang pengambilan data setelah sukses
            } else {
                Log.e("HapusHutang", "Gagal menghapus hutang dengan docId: $documentId")
            }
        }
    }

    fun ambilHutangSaya(userId: String) {
        viewModelScope.launch {
            firestore.collection("hutang")
                .whereEqualTo("id_penerima", userId)
                .get()
                .addOnSuccessListener { result ->
                    val daftarHutang = result.documents.mapNotNull { doc ->
                        val hutang = doc.toObject(Hutang::class.java)?.copy(docId = doc.id)
                        hutang?.let {
                            it.copy(
                                totalHutang = hitungTotalHutang(
                                    it.nominalpinjaman ?: 0.0,
                                    it.bunga ?: 0.0,
                                    it.lamaPinjaman ?: 0
                                )
                            )
                        }
                    }
                    Log.d("HutangViewModel", "Daftar hutang setelah hitung: $daftarHutang")
                    _hutangSayaList.value = daftarHutang
                }
                .addOnFailureListener {
                    Log.e("HutangViewModel", "Gagal ambil hutang", it)
                }
        }
    }

    fun ambilPiutangSaya(userId: String) {
        viewModelScope.launch {
            Log.d("debughutang", "Ambil piutang untuk userId: $userId")
            firestore.collection("hutang")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    val daftarPiutang = result.documents.mapNotNull { doc ->
                        val hutang = doc.toObject(Hutang::class.java)?.copy(docId = doc.id)
                        hutang?.let {
                            it.copy(
                                totalHutang = hitungTotalHutang(
                                    it.nominalpinjaman ?: 0.0,
                                    it.bunga ?: 0.0,
                                    it.lamaPinjaman ?: 0
                                )
                            )
                        }
                    }
                    Log.d("HutangViewModel", "Daftar piutang setelah hitung: $daftarPiutang")
                    _piutangSayaList.value = daftarPiutang
                }
                .addOnFailureListener {
                    Log.e("HutangViewModel", "Gagal ambil piutang", it)
                }
        }
    }
}