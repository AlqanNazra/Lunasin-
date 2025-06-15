package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Backend.Model.StatusBayar
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PiutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _piutangState = MutableStateFlow<Hutang?>(null)
    val piutangState: StateFlow<Hutang?> = _piutangState

    private val _piutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val piutangSayaList: StateFlow<List<Hutang>> = _piutangSayaList

    fun getPiutangById(docId: String) {
        viewModelScope.launch {
            try {
                Log.d("PiutangViewModel", "Mencari dokumen dengan docId: $docId")
                val document = firestore.collection("hutang").document(docId).get().await()
                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    var piutang = Hutang.fromMap(data).copy(docId = document.id)
                    // Hitung totalHutang
                    val calculatedTotalHutang = HutangCalculator.hitungTotalHutang(piutang)
                    piutang = piutang.copy(totalHutang = calculatedTotalHutang)
                    _piutangState.value = piutang
                    Log.d("PiutangViewModel", "Piutang ditemukan: docId=${piutang.docId}, totalHutang=$calculatedTotalHutang")
                } else {
                    _piutangState.value = null
                    Log.w("PiutangViewModel", "Dokumen tidak ditemukan untuk docId: $docId")
                }
            } catch (e: Exception) {
                _piutangState.value = null
                Log.e("PiutangViewModel", "Gagal mengambil piutang: ${e.message}", e)
            }
        }
    }

    fun clearPiutangState() {
        _piutangState.value = null
        Log.d("PiutangViewModel", "Hasil pencarian telah dihapus")
    }

    fun ambilPiutangSaya(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("PiutangViewModel", "Mengambil piutang untuk userId: $userId")
                val result = firestore.collection("hutang")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("type", "Piutang")
                    .get()
                    .await()
                Log.d("PiutangViewModel", "Dokumen ditemukan: ${result.documents.size}")
                val batch = firestore.batch()
                val daftarPiutang = result.documents.mapNotNull { doc ->
                    val data = doc.data ?: emptyMap()
                    var piutang = Hutang.fromMap(data).copy(docId = doc.id)
                    Log.d("PiutangViewModel", "Data mentah: ${doc.data}")
                    if (piutang.type == "Piutang" || data.containsKey("type") == false) {
                        // Hitung totalHutang
                        val calculatedTotalHutang = HutangCalculator.hitungTotalHutang(piutang)
                        piutang = piutang.copy(totalHutang = calculatedTotalHutang)
                        // Update Firestore hanya jika BELUM_LUNAS dan keterlambatan > 0
                        if (piutang.statusBayar == StatusBayar.BELUM_LUNAS &&
                            HutangCalculator.hitungKeterlambatan(piutang.tanggalJatuhTempo) > 0 &&
                            calculatedTotalHutang != (data["totalHutang"] as? Double)
                        ) {
                            batch.update(
                                firestore.collection("hutang").document(doc.id),
                                "totalHutang", calculatedTotalHutang
                            )
                            Log.d("PiutangViewModel", "Menjadwalkan update totalHutang: $calculatedTotalHutang untuk docId: ${doc.id}")
                        }
                        piutang
                    } else {
                        null
                    }
                }
                // Commit batch update
                if (daftarPiutang.isNotEmpty()) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("PiutangViewModel", "Batch update totalHutang berhasil untuk ${daftarPiutang.size} dokumen")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PiutangViewModel", "Gagal batch update totalHutang: ${e.message}")
                        }
                }
                _piutangSayaList.value = daftarPiutang
                Log.d("PiutangViewModel", "Daftar piutang setelah hitung: $daftarPiutang")
            } catch (e: Exception) {
                Log.e("PiutangViewModel", "Gagal ambil piutang: ${e.message}", e)
                _piutangSayaList.value = emptyList()
            }
        }
    }

    fun hapusPiutang(docId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("hutang").document(docId).delete().await()
                Log.d("PiutangViewModel", "Piutang dihapus: docId=$docId")
                onSuccess()
            } catch (e: Exception) {
                Log.e("PiutangViewModel", "Gagal hapus piutang: ${e.message}", e)
            }
        }
    }
}