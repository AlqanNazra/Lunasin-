package com.example.lunasin.Backend.Data.profile_data

import android.util.Log
import com.example.lunasin.Backend.Model.Profile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val profileCollection = firestore.collection("profile")

    suspend fun getProfile(userId: String): Profile {
        return try {
            val document = profileCollection.document(userId).get().await()
            if (document.exists()) {
                Profile.fromMap(document.data ?: emptyMap())
            } else {
                Profile() // Kembalikan profil kosong jika tidak ada
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Gagal mengambil profil: ${e.message}", e)
            Profile() // Kembalikan profil kosong jika gagal
        }
    }

    suspend fun saveProfile(userId: String, profile: Profile) {
        try {
            profileCollection.document(userId).set(profile.toMap()).await()
            Log.d("ProfileRepository", "Profil berhasil disimpan untuk userId: $userId")
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Gagal menyimpan profil: ${e.message}", e)
            throw e // Lempar exception untuk ditangani oleh ViewModel
        }
    }
}