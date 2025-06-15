package com.example.lunasin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Frontend.UI.navigation.NavGraph
import com.example.lunasin.Frontend.UI.navigation.Screen
import com.example.lunasin.Frontend.viewmodel.Authentifikasi.AuthViewModelFactory
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModelFactory
import com.example.lunasin.theme.LunasinTheme
import com.example.lunasin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var hutangViewModel: HutangViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Firebase
        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        val authFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        // Inisialisasi FirestoreService
        val firestoreService = FirestoreService()
        val hutangFactory = HutangViewModelFactory(firestoreService)
        hutangViewModel = ViewModelProvider(this, hutangFactory)[HutangViewModel::class.java]


        setContent {
            LunasinTheme {
                val authViewModel: AuthViewModel = viewModel()
                val hutangViewModel: HutangViewModel = viewModel()
                val startDestination = Screen.Login.route // atau sesuaikan dengan yang kamu butuhkan

                NavGraph(authViewModel, hutangViewModel, startDestination)
            }
        }
    }
}