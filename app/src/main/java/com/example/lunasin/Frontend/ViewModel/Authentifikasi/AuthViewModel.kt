package com.example.lunasin.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.lunasin.Frontend.UI.Navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

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
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                _errorMessage.value = "Verifikasi email telah dikirim. Silakan cek inbox Anda."
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            }
                            ?.addOnFailureListener {
                                _errorMessage.value = "Gagal mengirim email verifikasi."
                            }
                    } else {
                        _errorMessage.value = task.exception?.message
                    }
                }
        }
    }


    fun signIn(email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            _isAuthenticated.value = true
                        } else {
                            _errorMessage.value = "Email belum diverifikasi. Silakan cek inbox Anda."
                            auth.signOut() // logout agar user tidak tetap login
                        }
                    } else {
                        _errorMessage.value = task.exception?.message
                    }
                }
        }
    }

    // Di AuthViewModel.kt
    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer

    fun startResendTimer(duration: Int = 60) {
        viewModelScope.launch {
            for (i in duration downTo 0) {
                _resendTimer.value = i
                delay(1000)
            }
        }
    }

    fun resendVerificationEmail(context: Context) {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Email verifikasi dikirim ulang.", Toast.LENGTH_SHORT).show()
                    startResendTimer()
                } else {
                    setError("Gagal mengirim ulang email verifikasi")
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

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

}