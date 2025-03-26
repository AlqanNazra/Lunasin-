package com.example.lunasin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.lunasin.navigation.NavGraph
import com.example.lunasin.viewmodel.AuthViewModel
import com.example.lunasin.viewmodel.AuthViewModelFactory
import com.example.lunasin.data.AuthRepository
import com.example.lunasin.theme.LunasinTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setContent {
            LunasinTheme {
                NavGraph(authViewModel)
            }
        }
    }
}