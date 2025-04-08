package com.example.lunasin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.navigation.NavController
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.lunasin.Frontend.UI.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

open class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        // Memeriksa apakah user masih terautentikasi saat aplikasi dibuka
        val currentUser = auth.currentUser
        _isAuthenticated.value = currentUser != null
    }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun signUp(email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            val result = authRepo.signUp(email, password)
            _isAuthenticated.value = result != null
            if (result != null) {
                navController.navigate(Screen.Home.route)
            }
        }
    }

    fun signIn(email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            val result = authRepo.signIn(email, password)
            if (result?.user != null) {
                _isAuthenticated.value = true
                Log.d("AuthViewModel", "Login sukses: userId = ${result.user?.uid}")
                navController.navigate(Screen.Home.route)
            } else {
                _errorMessage.value = "Login gagal! Periksa kembali email dan password."
            }
        }
    }


    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val result = authRepo.signInWithGoogle(idToken)
            _isAuthenticated.value = result != null
        }
    }

    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "Password reset email sent!")
                } else {
                    onComplete(false, task.exception?.message ?: "Unknown error occurred")
                }
            }
    }



    fun signOut(navController: NavController) {
        try {
            authRepo.signOut()  // Memanggil fungsi sign out dari repositori
            _isAuthenticated.value = false  // Mengubah status autentikasi
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }  // Menghapus semua rute sebelumnya
                launchSingleTop = true  // Menjaga agar halaman login hanya muncul satu kali
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error during signOut", e)
            // Tampilkan error atau beri tahu pengguna jika terjadi kesalahan
        }
    }



    fun setError(s: String) {
        _errorMessage.value = s
    }

}