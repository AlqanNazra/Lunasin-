package com.example.lunasin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.navigation.NavController
import com.example.lunasin.data.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.lunasin.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

open class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

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
            if (result == null) {
                _errorMessage.value = "Login gagal! Periksa kembali email dan password."
            } else {
                _isAuthenticated.value = true
                navController.navigate(Screen.Home.route)
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



    fun signOut() {
        authRepo.signOut()
        _isAuthenticated.value = false
    }

    fun setError(s: String) {
        _errorMessage.value = s
    }

}