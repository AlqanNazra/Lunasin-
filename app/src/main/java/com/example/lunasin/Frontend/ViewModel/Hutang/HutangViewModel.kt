package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel untuk mengelola data dan logika terkait Hutang.
 * Bertanggung jawab untuk operasi seperti pengambilan, penambahan, penghitungan, dan penghapusan hutang.
 */
class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    // State untuk menyimpan daftar hutang saya
    private val _hutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val hutangSayaList: StateFlow<List<Hutang>> = _hutangSayaList

    // State untuk menyimpan hasil pencarian hutang berdasarkan ID
    private val _hutangState = MutableStateFlow<Hutang?>(null)
    val hutangState: StateFlow<Hutang?> = _hutangState

    private val firestore = FirebaseFirestore.getInstance()

    // Fungsi untuk mengambil hutang berdasarkan ID
    fun getHutangById(docId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("hutang").document(docId).get().await()
                if (document.exists()) {
                    val hutang = Hutang.fromMap(document.data ?: emptyMap()).copy(docId = docId)
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

    // Fungsi untuk mengosongkan hasil pencarian hutang
    fun clearHutangState() {
        _hutangState.value = null
        Log.d("HutangViewModel", "Hasil pencarian telah dihapus")
    }

    // Fungsi untuk mengklaim hutang
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

    // Fungsi untuk mengambil daftar hutang saya berdasarkan userId
    fun ambilHutangSaya(userId: String) {
        viewModelScope.launch {
            try {
                val result = firestore.collection("hutang")
                    .whereEqualTo("id_penerima", userId)
                    .get()
                    .await()
                val daftarHutang = result.documents.mapNotNull { doc ->
                    val hutang = Hutang.fromMap(doc.data ?: emptyMap()).copy(docId = doc.id)
                    hutang.let {
                        it.copy(
                            totalHutang = HutangCalculator.hitungTotalHutang(
                                it.nominalpinjaman,
                                it.bunga,
                                it.lamaPinjaman
                            )
                        )
                    }
                }
                _hutangSayaList.value = daftarHutang
                Log.d("HutangViewModel", "Daftar hutang setelah hitung: $daftarHutang")
            } catch (e: Exception) {
                Log.e("HutangViewModel", "Gagal ambil hutang", e)
            }
        }
    }

    // Fungsi untuk menambahkan hutang baru
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

    // Fungsi untuk menghitung dan menyimpan hutang baru
    fun hitungDanSimpanHutang(
        type: String, // Menambahkan parameter type untuk membedakan Hutang/Piutang
        hutangType: HutangType,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double,
        lamaPinjam: Int,
        tanggalPinjam: String,
        catatan: String,
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

        val hutangData = when (hutangType) {
            HutangType.SERIUS -> createHutangSeriusData(
                userId, namapinjaman, nominalpinjaman, bunga, lamaPinjam, tanggalPinjam, catatan, calendar, sdf
            )
            HutangType.TEMAN -> createHutangTemanData(
                userId, namapinjaman, nominalpinjaman, tanggalPinjam, catatan
            )
            HutangType.PERHITUNGAN -> createHutangPerhitunganData(
                userId, namapinjaman, nominalpinjaman, bunga, tanggalPinjam, catatan
            )
        }

        // Tambahkan field type ke data yang disimpan
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

    // Fungsi untuk menghapus hutang berdasarkan documentId
    fun hapusHutang(documentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = firestoreService.hapusHutang(documentId)
            if (result) {
                Log.d("HapusHutang", "Hutang dengan docId $documentId berhasil dihapus.")
                onSuccess()
            } else {
                Log.e("HapusHutang", "Gagal menghapus hutang dengan docId: $documentId")
            }
        }
    }

    // Helper function untuk membuat data hutang tipe SERIUS
    private fun createHutangSeriusData(
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
            "hutangType" to HutangType.SERIUS.name // Simpan jenis hutang
        )
    }

    // Helper function untuk membuat data hutang tipe TEMAN
    private fun createHutangTemanData(
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        tanggalPinjam: String,
        catatan: String
    ): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "tanggalPinjam" to tanggalPinjam,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.TEMAN.name // Simpan jenis hutang
        )
    }

    // Helper function untuk membuat data hutang tipe PERHITUNGAN
    private fun createHutangPerhitunganData(
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
            "hutangType" to HutangType.PERHITUNGAN.name // Simpan jenis hutang
        )
    }

    // Helper function untuk membuat daftar tempo
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
}