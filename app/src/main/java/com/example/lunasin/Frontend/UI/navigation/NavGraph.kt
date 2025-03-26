package com.example.lunasin.Frontend.UI.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.lunasin.Frontend.UI.Inputhutang.InputHutangScreen
import com.example.lunasin.Frontend.UI.login.*
import com.example.lunasin.Frontend.viewmodel.Authentifikasi.AuthViewModel
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.UI.Inputhutang.PreviewHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.TanggalTempoScreen
import com.example.lunasin.Frontend.UI.Inputhutang.ListHutangScreen


@Composable
fun NavGraph(authViewModel: AuthViewModel, hutangViewModel: HutangViewModel, startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list_hutang_screen"
    ) {
        composable("login_screen") { LoginScreen(authViewModel, navController) }
        composable("input_hutang_screen") {
            InputHutangScreen(hutangViewModel, navController)
        }
        composable("preview_hutang/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            PreviewHutangScreen(hutangViewModel, navController, docId)
        }
        // 🔥 Tambahkan route untuk TanggalTempoScreen
        composable("tanggalTempo/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            TanggalTempoScreen(hutangViewModel, navController, docId)
        }
        composable("list_hutang_screen") { ListHutangScreen(hutangViewModel, navController) }


    }
}
