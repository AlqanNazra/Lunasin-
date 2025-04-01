package com.example.lunasin.Frontend.UI.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.lunasin.Frontend.UI.login.*
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.UI.Inputhutang.*
import com.example.lunasin.ui.screens.ForgotPasswordScreen
import com.example.lunasin.ui.screens.LoginScreen
import com.example.lunasin.ui.screens.SignUpScreen
import com.example.lunasin.viewmodel.AuthViewModel
import com.example.lunasin.Frontend.UI.Home.*

@Composable
fun NavGraph(authViewModel: AuthViewModel, hutangViewModel: HutangViewModel, startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel, navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(authViewModel, navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(authViewModel, navController)
        }
        composable(Screen.ForgotScreen.route) {
            ForgotPasswordScreen(navController, authViewModel::resetPassword)
        }
        composable("input_hutang_screen") {
            SeriusHutangScreen(hutangViewModel, navController)
        }
        composable("perhitungan_hutang_screen") {
            PerhitunganHutangScreen(hutangViewModel, navController)
        }
        composable("teman_hutang_screen") {
            TemanHutangScreen(hutangViewModel, navController)
        }
        composable("teman_preview_hutang/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            TemanPreviewHutangScreen(hutangViewModel, navController, docId)
        }
        composable("perhitungan_preview_hutang/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            PerhitunganPreviewHutangScreen(hutangViewModel, navController, docId)
        }
        composable("preview_hutang/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            SeriusPreviewHutangScreen(hutangViewModel, navController, docId)
        }
        composable("tanggalTempo/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            TanggalTempoScreen(hutangViewModel, navController, docId)
        }
        composable("input_hutang_teman_screen") {
            PilihHutangScreen(navController) }
        composable("list_hutang_screen") { ListHutangScreen(hutangViewModel, navController) }

        composable("home_screen") {
            HomeScreen(navController)
        }

    }
}
