package com.example.lunasin.Frontend.UI.Navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lunasin.Frontend.UI.Home.HomeScreen
import com.example.lunasin.Frontend.UI.Hutang.Hutang.ListUtangScreen
import com.example.lunasin.Frontend.UI.Hutang.Hutang.PreviewPiutangPerhitunganScreen
import com.example.lunasin.Frontend.UI.Hutang.Hutang.PreviewPiutangScreen
import com.example.lunasin.Frontend.UI.Hutang.Hutang.PreviewUtangPerhitunganScreen
import com.example.lunasin.Frontend.UI.Hutang.Hutang.PreviewUtangScreen
import com.example.lunasin.Frontend.UI.Hutang.Piutang.ListPiutangScreen
import com.example.lunasin.Frontend.UI.Hutang.Piutang.PerhitunganPiutangScreen
import com.example.lunasin.Frontend.UI.Hutang.Piutang.PilihPiutangScreen
import com.example.lunasin.Frontend.UI.Hutang.Piutang.TemanPiutangScreen
import com.example.lunasin.Frontend.UI.Hutang.TanggalTempoScreen
import com.example.lunasin.Frontend.UI.Profile.ProfileScreen
import com.example.lunasin.Frontend.UI.Statistik.StatisticsScreen
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModel
import com.example.lunasin.Frontend.ViewModel.Profile.ProfileViewModel
import com.example.lunasin.Frontend.ViewModel.Profile.ProfileViewModelFactory
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
        startDestination = startDestination // Gunakan startDestination yang diberikan
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
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
            ProfileScreen(navController, authViewModel, profileViewModel)
        }

        // Input Piutang
        composable("pilih_piutang") {
            PilihPiutangScreen(navController)
        }
        composable("input_piutang_teman") {
            TemanPiutangScreen(hutangViewModel, navController)
        }
        composable("input_piutang_perhitungan") {
            PerhitunganPiutangScreen(hutangViewModel, navController)
        }

        // Preview Piutang
        composable("piutang_teman_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            PreviewPiutangScreen(
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

        // Preview Hutang
        composable("hutang_teman_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PreviewUtangScreen(hutangViewModel, navController, docId, userId)
        }
        composable("hutang_perhitungan_preview/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PreviewUtangPerhitunganScreen(hutangViewModel, navController, docId, userId)
        }

        composable("tanggalTempo/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            TanggalTempoScreen(hutangViewModel, navController, docId)
        }

        // Daftar Hutang dan Piutang
        composable("list_hutang_screen") {
            ListPiutangScreen(piutangViewModel, navController)
        }
        composable("list_utang_screen") {
            ListUtangScreen(hutangViewModel, navController)
        }

        // Statistika (Ditambahkan oleh saya)
        composable("stats_screen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            StatisticsScreen(userId = userId)
        }
    }
}

// Enum untuk rute (diperbarui dengan penambahan Stats)
//sealed class Screen(val route: String) {
//    object Login : Screen("login")
//    object SignUp : Screen("sign_up")
//    object ForgotScreen : Screen("forgot_password")
//    object Profile : Screen("profile")
//    object Stats : Screen("stats_screen")
//}