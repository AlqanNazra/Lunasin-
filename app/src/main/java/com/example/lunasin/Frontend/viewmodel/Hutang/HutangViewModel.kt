package com.example.lunasin.Frontend.viewmodel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator.hitungCicilanPerbulan
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator.hitungDendaTetap
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator.hitungTanggalAkhir
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangCalculator.hitungTotalHutang
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {

    enum class HutangType { SERIUS, TEMAN, PERHITUNGAN }

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
                    // Dengan @DocumentId di Hutang.kt, ini akan mengisi docId secara otomatis
                    val hutang = document.toObject<Hutang>()
                    _hutangState.value = hutang
                    Log.d("FirestoreDebug", "Data hutang berhasil didapat: $hutang")
                } else {
                    Log.e("Firestore", "Dokumen tidak ditemukan!")
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Gagal mengambil data", e)
            }
        }
    }

    fun klaimHutang(idHutang: String, idPenerima: String) {
        val hutangRef = firestore.collection("hutang").document(idHutang)
        hutangRef.update("id_penerima", idPenerima)
            .addOnSuccessListener { Log.d("KlaimHutang", "Hutang berhasil diklaim oleh $idPenerima") }
            .addOnFailureListener { e -> Log.e("KlaimHutang", "Gagal klaim hutang", e) }
    }

    fun ambilDataHutang(userId: String) {
        viewModelScope.launch {
            firestore.collection("hutang")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    _hutangList.value = result.documents.mapNotNull { it.toObject<Hutang>() }
                }
                .addOnFailureListener { e -> Log.e("HutangViewModel", "Gagal mengambil data hutang", e) }
        }
    }

    fun hitungDanSimpanHutang(
        hutangType: HutangType,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjamString: String,
        catatan: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(false, null)
            return
        }
        val userId = user.uid

        val formatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val tanggalPinjamDate: Date = try {
            formatter.parse(tanggalPinjamString) ?: run {
                onResult(false, null)
                return
            }
        } catch (e: Exception) {
            onResult(false, null)
            return
        }

        val calendar = Calendar.getInstance().apply { time = tanggalPinjamDate }
        val db = FirebaseFirestore.getInstance()

        val hutangData: Map<String, Any> = when (hutangType) {
            HutangType.SERIUS -> {
                val tanggalBayarDate = hitungTanggalAkhir(calendar, lamaPinjam)
                val totalHutang = hitungTotalHutang(nominalpinjaman, bunga, lamaPinjam)
                val totalCicilan = hitungCicilanPerbulan(nominalpinjaman, bunga, lamaPinjam)
                val listTempo = createTempoList(lamaPinjam, calendar, totalCicilan)
                mapOf(
                    "userId" to userId,
                    "namapinjaman" to namapinjaman,
                    "nominalpinjaman" to nominalpinjaman,
                    "bunga" to bunga,
                    "lamaPinjaman" to lamaPinjam,
                    "totalHutang" to totalHutang,
                    "tanggalPinjam" to tanggalPinjamDate,
                    "tanggalBayar" to tanggalBayarDate,
                    "listTempo" to listTempo,
                    "catatan" to catatan,
                    "totalcicilan" to totalCicilan,
                    "id_penerima" to ""
                )
            }
            HutangType.TEMAN -> {
                mapOf(
                    "userId" to userId,
                    "namapinjaman" to namapinjaman,
                    "nominalpinjaman" to nominalpinjaman,
                    "tanggalPinjam" to tanggalPinjamDate,
                    "catatan" to catatan,
                    "id_penerima" to ""
                )
            }
            HutangType.PERHITUNGAN -> {
                val tanggalBayarDate = hitungTanggalAkhir(calendar, lamaPinjam)
                val totalHutang = hitungDendaTetap(nominalpinjaman, bunga)
                val listTempo = createTempoList(lamaPinjam, calendar, 0.0)
                mapOf(
                    "userId" to userId,
                    "namapinjaman" to namapinjaman,
                    "nominalpinjaman" to nominalpinjaman,
                    "totalHutang" to totalHutang,
                    "tanggalPinjam" to tanggalPinjamDate,
                    "tanggalBayar" to tanggalBayarDate,
                    "listTempo" to listTempo,
                    "catatan" to catatan,
                    "id_penerima" to ""
                )
            }
        }

        // --- INI BAGIAN YANG DIPERBAIKI ---
        db.collection("hutang").add(hutangData)
            .addOnSuccessListener { documentReference ->
                // Kita tidak lagi melakukan .update() untuk menyimpan "docId" secara manual.
                // Cukup kembalikan ID yang didapat dari documentReference.
                Log.d("FirestoreSuccess", "Hutang berhasil disimpan dengan docId: ${documentReference.id}")
                onResult(true, documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Gagal menyimpan data hutang", e)
                onResult(false, null)
            }
    }

    private fun createTempoList(
        lamaPinjam: Int,
        startCalendar: Calendar,
        amountPerAngsuran: Double
    ): List<Map<String, Any?>> {
        val listTempo = mutableListOf<Map<String, Any?>>()
        val calendar = startCalendar.clone() as Calendar
        for (i in 1..lamaPinjam) {
            calendar.add(Calendar.MONTH, 1)
            listTempo.add(
                mapOf(
                    "angsuranKe" to i,
                    "tanggalTempo" to calendar.time,
                    "amount" to amountPerAngsuran,
                    "paid" to false,
                    "paymentDate" to null
                )
            )
        }
        return listTempo
    }

    fun hapusHutang(documentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (firestoreService.hapusHutang(documentId)) {
                onSuccess()
            }
        }
    }

    fun ambilHutangSaya(userId: String) {
        viewModelScope.launch {
            firestore.collection("hutang")
                .whereEqualTo("id_penerima", userId)
                .get()
                .addOnSuccessListener { result ->
                    _hutangSayaList.value = result.documents.mapNotNull { it.toObject<Hutang>() }
                }
                .addOnFailureListener { e -> Log.e("HutangViewModel", "Gagal ambil hutang saya", e) }
        }
    }

    fun ambilPiutangSaya(userId: String) {
        viewModelScope.launch {
            Log.d("debughutang", "Ambil piutang untuk userId: $userId")
            firestore.collection("hutang")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    _piutangSayaList.value = result.documents.mapNotNull { it.toObject<Hutang>() }
                }
                .addOnFailureListener { e -> Log.e("HutangViewModel", "Gagal ambil piutang saya", e) }
        }
    }
}