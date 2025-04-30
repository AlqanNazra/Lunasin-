package com.example.lunasin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Frontend.UI.Navigation.NavGraph
import com.example.lunasin.Frontend.UI.Navigation.Screen
import com.example.lunasin.Frontend.ViewModel.Authentifikasi.AuthViewModelFactory
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModelFactory
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModel
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModelFactory
import com.example.lunasin.theme.LunasinTheme
import com.example.lunasin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var hutangViewModel: HutangViewModel
    private lateinit var piutangViewModel: PiutangViewModel

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

        // Inisialisasi PiutangViewModel
        val piutangFactory = PiutangViewModelFactory(firestoreService)
        piutangViewModel = ViewModelProvider(this, piutangFactory)[PiutangViewModel::class.java]

        setContent {
            LunasinTheme {
                val startDestination = Screen.Login.route
                NavGraph(authViewModel, hutangViewModel, piutangViewModel, startDestination)
            }
        }
    }
}