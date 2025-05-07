package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                val daftarPiutang = result.documents.mapNotNull { doc ->
                    val piutang = Hutang.fromMap(doc.data ?: emptyMap()).copy(docId = doc.id)
                    Log.d("PiutangViewModel", "Data mentah: ${doc.data}")
                    if (piutang.type == "Piutang" || doc.data?.containsKey("type") == false) {
                        piutang.let {
                            val updatedPiutang = when (it.hutangType) {
                                HutangType.SERIUS -> it.copy(
                                    totalHutang = HutangCalculator.hitungTotalHutang(
                                        it.nominalpinjaman,
                                        it.bunga,
                                        it.lamaPinjaman
                                    )
                                )
                                HutangType.TEMAN -> it.copy(
                                    totalHutang = it.nominalpinjaman
                                )
                                HutangType.PERHITUNGAN -> it.copy(
                                    totalHutang = it.nominalpinjaman + (it.totalDenda ?: 0.0)
                                )
                                else -> it
                            }
                            Log.d("PiutangViewModel", "Piutang setelah perhitungan: $updatedPiutang")
                            updatedPiutang
                        }
                    } else {
                        null
                    }
                }
                _piutangSayaList.value = daftarPiutang
                Log.d("PiutangViewModel", "Daftar piutang setelah hitung: $daftarPiutang")
            } catch (e: Exception) {
                Log.e("PiutangViewModel", "Gagal ambil piutang", e)
                _piutangSayaList.value = emptyList()
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