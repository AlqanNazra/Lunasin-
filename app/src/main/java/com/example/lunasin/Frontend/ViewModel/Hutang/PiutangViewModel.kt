package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class PiutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    private val _piutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val piutangSayaList: StateFlow<List<Hutang>> = _piutangSayaList

    private val _piutangState = MutableStateFlow<Hutang?>(null)
    val piutangState: StateFlow<Hutang?> = _piutangState

    private val firestore = FirebaseFirestore.getInstance()

    // Fungsi untuk mengambil piutang berdasarkan ID
    fun getPiutangById(docId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("hutang").document(docId).get().await()
                if (document.exists()) {
                    val piutang = Hutang.fromMap(document.data ?: emptyMap()).copy(docId = docId)
                    _piutangState.value = piutang
                    Log.d("FirestoreDebug", "Data piutang berhasil didapat: $piutang")
                } else {
                    _piutangState.value = null
                    Log.e("Firestore", "Dokumen tidak ditemukan!")
                }
            } catch (e: Exception) {
                _piutangState.value = null
                Log.e("Firestore", "Gagal mengambil data", e)
            }
        }
    }

    // Fungsi untuk mengosongkan hasil pencarian piutang
    fun clearPiutangState() {
        _piutangState.value = null
        Log.d("PiutangViewModel", "Hasil pencarian telah dihapus")
    }

    fun simpanPiutang(
        hutangType: HutangType,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double = 0.0,
        lamaPinjam: Int = 0,
        tanggalPinjam: String,
        catatan: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("FirestoreError", "User belum login, tidak bisa menyimpan piutang")
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

        val piutangData = when (hutangType) {
            HutangType.SERIUS -> createPiutangSeriusData(
                userId, namapinjaman, nominalpinjaman, bunga, lamaPinjam, tanggalPinjam, catatan, calendar, sdf
            )
            HutangType.TEMAN -> createPiutangTemanData(
                userId, namapinjaman, nominalpinjaman, tanggalPinjam, catatan
            )
            HutangType.PERHITUNGAN -> createPiutangPerhitunganData(
                userId, namapinjaman, nominalpinjaman, bunga, tanggalPinjam, catatan
            )
        }

        firestore.collection("piutang").add(piutangData)
            .addOnSuccessListener { documentReference ->
                val docId = documentReference.id
                documentReference.update("docId", docId)
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Piutang berhasil disimpan dengan docId: $docId")
                        onResult(true, docId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Gagal menyimpan docId", e)
                        onResult(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Gagal menyimpan data piutang", e)
                onResult(false, null)
            }
    }

    private fun createPiutangTemanData(
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        tanggalPinjam: String,
        catatan:

        String
    ): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "tanggalPinjam" to tanggalPinjam,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.TEMAN.name // Simpan jenis piutang
        )
    }

    private fun createPiutangPerhitunganData(
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        tanggalPinjam: String,
        catatan: String
    ): Map<String, Any?> {
        val denda = HutangCalculator.dendaTetap(nominalpinjaman, bunga)
        return mapOf(
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "totalDenda" to denda,
            "tanggalPinjam" to tanggalPinjam,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.PERHITUNGAN.name // Simpan jenis piutang
        )
    }

    private fun createPiutangSeriusData(
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
        val tanggalBayar = HutangCalculator.hitungTanggalAkhir(tanggalPinjam, lamaPinjam)
        val totalHutang = HutangCalculator.hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
        val listTempo = createTempoList(lamaPinjam, calendar, sdf)
        val totalCicilan = HutangCalculator.cicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)

        return mapOf(
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
            "totalcicilan" to totalCicilan,
            "id_penerima" to "",
            "hutangType" to HutangType.SERIUS.name // Simpan jenis piutang
        )
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

    fun ambilPiutangSaya(userId: String) {
        viewModelScope.launch {
            try {
                val result = firestore.collection("hutang")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val daftarPiutang = result.documents.mapNotNull { doc ->
                    Hutang.fromMap(doc.data ?: emptyMap()).copy(docId = doc.id)
                }
                _piutangSayaList.value = daftarPiutang
            } catch (e: Exception) {
                Log.e("PiutangViewModel", "Gagal ambil piutang", e)
            }
        }
    }

    fun hapusPiutang(docId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("hutang").document(docId).delete().await()
                onSuccess()
            } catch (e: Exception) {
                Log.e("PiutangViewModel", "Gagal menghapus piutang", e)
            }
        }
    }
}