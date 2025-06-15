package com.example.lunasin.Backend.Data.login_Data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {

    suspend fun signUp(email: String, password: String): AuthResult? {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult? {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult? {
        return try {
            Log.d("AuthRepo", "Processing token: $idToken")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            Log.d("AuthRepo", "Sign-in successful: ${authResult.user?.uid}")
            authResult
        } catch (e: Exception) {
            Log.e("AuthRepo", "Sign-in failed: ${e.message}", e)
            null
        }
    }


    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}