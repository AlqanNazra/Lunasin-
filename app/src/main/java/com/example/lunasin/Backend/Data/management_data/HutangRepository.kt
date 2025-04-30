package com.example.lunasin.Backend.Data.management_data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.lunasin.Backend.Model.Hutang

class HutangRepository(private val firestore: FirebaseFirestore) {

    private val hutangCollection = firestore.collection("hutang") // Ganti sesuai struktur Firestore

    suspend fun tambahHutang(hutang: Hutang) {
        hutangCollection.add(hutang).await()
    }

    suspend fun getDaftarHutang(): List<Hutang> {
        return try {
            val snapshot = hutangCollection.get().await()
            snapshot.toObjects(Hutang::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}