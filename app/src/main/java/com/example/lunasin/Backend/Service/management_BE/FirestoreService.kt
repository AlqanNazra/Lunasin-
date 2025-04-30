package com.example.lunasin.Backend.Service.management_BE

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.lunasin.Backend.Model.Hutang

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val hutangCollection = firestore.collection("hutang") // 🔥 Tambahkan deklarasi ini

    suspend fun tambahHutang(hutang: Map<String, Any>): String? {
        return try {
            val documentRef = firestore.collection("hutang").add(hutang).await()
            documentRef.id  //
        } catch (e: Exception) {
            Log.e("FirestoreError", "Gagal menambahkan hutang", e)
            null
        }
    }

    fun simpanHutang(hutang: Hutang, onComplete: (Boolean) -> Unit) {
        firestore.collection("hutang")
            .add(hutang)
            .addOnSuccessListener {
                Log.d("FirestoreService", "Hutang berhasil disimpan dengan ID: ${it.id}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "Gagal menyimpan hutang", e)
                onComplete(false)
            }
    }

    suspend fun getHutang(): List<Hutang>? {
        return try {
            val snapshot = firestore.collection("hutang")
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents.map { document ->
                    document.toObject(Hutang::class.java)?.copy(docId = document.id)
                }.filterNotNull()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Gagal mengambil data hutang", e)
            emptyList()
        }
    }


    suspend fun getHutangById(docId: String): Hutang? {
        return try {
            val document = firestore.collection("hutang").document(docId).get().await()
            document.toObject(Hutang::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error mengambil hutang", e)
            null
        }
    }
    suspend fun hapusHutang(documentId: String): Boolean {
        return try {
            hutangCollection.document(documentId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

}
