package com.example.lunasin.Frontend.UI.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModel
import com.example.lunasin.Frontend.UI.Hutang.Hutang.*
import com.example.lunasin.Frontend.UI.Hutang.Piutang.*
import com.example.lunasin.Frontend.UI.Home.*
import com.example.lunasin.Frontend.UI.Hutang.TanggalTempoScreen
import com.example.lunasin.Frontend.UI.Profile.ProfileScreen
import com.example.lunasin.ui.screens.ForgotPasswordScreen
import com.example.lunasin.ui.screens.LoginScreen
import com.example.lunasin.ui.screens.SignUpScreen
import com.example.lunasin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    hutangViewModel: HutangViewModel,
    piutangViewModel: PiutangViewModel,
    startDestination: String
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Autentikasi
        composable(Screen.Login.route) {
            LoginScreen(authViewModel, navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(authViewModel, navController)
        }
        composable(Screen.ForgotScreen.route) {
            ForgotPasswordScreen(navController, authViewModel::resetPassword)
        }

        // Home
        composable("home_screen") {
            HomeScreen(navController, hutangViewModel, piutangViewModel)
        }

        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        // Input Piutang
        composable("pilih_piutang") {
            PilihPiutangScreen(navController)
        }
        composable("input_piutang_teman") {
            TemanPiutangScreen(hutangViewModel, navController)
        }
        composable("input_piutang_serius") {
            SeriusPiutangScreen(hutangViewModel, navController)
        }
        composable("input_piutang_perhitungan") {
            PerhitunganPiutangScreen(hutangViewModel, navController)
        }

        // Preview Piutang (sesuai dengan navigasi di screen input)
        composable("piutang_teman_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            PreviewPiutangScreen(
                docId = docId,
                viewModel = hutangViewModel,
                navController = navController
            )
        }
        composable("piutang_serius_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            PreviewPiutangSeriusScreen(
                docId = docId,
                viewModel = hutangViewModel,
                navController = navController
            )
        }
        composable("piutang_perhitungan_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            PreviewPiutangPerhitunganScreen(
                docId = docId,
                viewModel = hutangViewModel,
                navController = navController
            )
        }

        // Preview Hutang (yang dibuat otomatis dari Piutang pengguna lain)
        composable("hutang_teman_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PreviewUtangScreen(hutangViewModel, navController, docId, userId )
        }
        composable("hutang_perhitungan_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PreviewUtangPerhitunganScreen(hutangViewModel, navController, docId, userId)
        }
        composable("hutang_serius_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PreviewUtangSeriusScreen(
                viewModel = hutangViewModel,
                navController = navController,
                docId = docId,
                userId = userId
            )
        }
        composable("tanggalTempo/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            TanggalTempoScreen(hutangViewModel, navController, docId)
        }

        // Daftar Hutang dan Piutang
        composable("list_hutang_screen") {
            ListPiutangScreen(piutangViewModel, navController) // Menggunakan PiutangViewModel
        }
        composable("list_utang_screen") {
            ListUtangScreen(hutangViewModel, navController)
        }
    }
}