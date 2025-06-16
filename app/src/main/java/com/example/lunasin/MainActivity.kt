package com.example.lunasin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
// Dihapus: import androidx.lifecycle.viewmodel.compose.viewModel (tidak diperlukan lagi di sini)
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Frontend.UI.navigation.NavGraph
// Dihapus: import com.example.lunasin.Frontend.UI.navigation.Screen (tidak diperlukan lagi di sini)
import com.example.lunasin.Frontend.viewmodel.Authentifikasi.AuthViewModelFactory
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModelFactory
import com.example.lunasin.theme.LunasinTheme
import com.example.lunasin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    // ViewModel didefinisikan sebagai properti kelas
    private lateinit var authViewModel: AuthViewModel
    private lateinit var hutangViewModel: HutangViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi ViewModel DILAKUKAN SEKALI DI SINI dengan factory yang benar
        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        val authFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        val firestoreService = FirestoreService()
        val hutangFactory = HutangViewModelFactory(firestoreService)
        hutangViewModel = ViewModelProvider(this, hutangFactory)[HutangViewModel::class.java]

        setContent {
            LunasinTheme {
                // --- PERUBAHAN DI SINI ---
                // Tidak perlu membuat ViewModel baru di sini.
                // Langsung gunakan ViewModel yang sudah diinisialisasi di `onCreate`.

                // Memanggil NavGraph dengan DUA argumen yang benar.
                NavGraph(
                    authViewModel = this.authViewModel,
                    hutangViewModel = this.hutangViewModel
                )
            }
        }
    }
}