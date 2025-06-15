package com.example.lunasin.Backend.Data.management_data

import android.util.Log
import com.example.lunasin.Backend.model.Hutang
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class HutangRepository(private val firestore: FirebaseFirestore) {

    private val hutangCollection = firestore.collection("hutang")
    private val auth = FirebaseAuth.getInstance()

    suspend fun tambahHutang(hutang: Hutang) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HutangRepo", "User not logged in, cannot add hutang.")
            return
        }
        hutangCollection.add(hutang).await() // Firestore akan otomatis menangani Hutang dan listTempo (nested)
    }

    suspend fun getDaftarHutang(): List<Hutang> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HutangRepo", "User not logged in.")
            return emptyList()
        }
        return try {
            val snapshot = hutangCollection.whereEqualTo("userId", userId).get().await()
            snapshot.toObjects(Hutang::class.java) // Langsung mengonversi ke List<Hutang>
        } catch (e: Exception) {
            Log.e("HutangRepo", "Error getting debt list: ${e.message}", e)
            emptyList()
        }
    }

    // Fungsi ini tidak diperlukan lagi jika kita mengambil semua data lalu filter di client
    // Namun, jika Anda punya kasus penggunaan lain, Anda bisa biarkan, tapi ingat:
    // Filtering String tanggal TIDAK AKURAT. Jika ini tetap ada, pastikan tanggal di Firestore sudah Timestamp.
    /*
    suspend fun getHutangByTanggalPinjam(startDate: Date, endDate: Date): List<Hutang> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HutangRepo", "User not logged in.")
            return emptyList()
        }
        return try {
            val snapshot = hutangCollection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("tanggalPinjam", startDate)
                .whereLessThanOrEqualTo("tanggalPinjam", endDate)
                .orderBy("tanggalPinjam", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(Hutang::class.java)
        } catch (e: Exception) {
            Log.e("HutangRepo", "Error getting debt by tanggalPinjam: ${e.message}", e)
            emptyList()
        }
    }
    */

    suspend fun bayarAngsuran(docId: String, angsuranKe: Int, paymentDate: Date) { // Sekarang menerima Date
        try {
            val document = hutangCollection.document(docId).get().await()
            if (document.exists()) {
                val hutang = document.toObject(Hutang::class.java)
                if (hutang != null) {
                    val updatedListTempo = hutang.listTempo.map { tempo ->
                        if (tempo.angsuranKe == angsuranKe) {
                            tempo.copy(paid = true, paymentDate = paymentDate) // Gunakan copy untuk update
                        } else {
                            tempo
                        }
                    }
                    // Firestore akan otomatis mengonversi List<Tempo> ke array of maps di Firestore,
                    // dan Date di Tempo ke Timestamp.
                    hutangCollection.document(docId).update("listTempo", updatedListTempo).await()
                    Log.d("Pembayaran", "Angsuran ke-$angsuranKe berhasil dibayar pada $paymentDate")
                } else {
                    Log.e("Pembayaran", "Gagal mengkonversi dokumen ke Hutang")
                }
            } else {
                Log.e("Pembayaran", "Dokumen hutang tidak ditemukan")
            }
        } catch (e: Exception) {
            Log.e("Pembayaran", "Gagal memperbarui status pembayaran", e)
        }
    }
}