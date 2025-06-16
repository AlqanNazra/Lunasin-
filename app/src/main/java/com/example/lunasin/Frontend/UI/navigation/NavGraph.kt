package com.example.lunasin.Frontend.UI.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lunasin.Backend.Data.management_data.HutangRepository
import com.example.lunasin.Backend.Data.profile_data.ProfileRepository
import com.example.lunasin.Frontend.UI.Home.HomeScreen
import com.example.lunasin.Frontend.UI.Inputhutang.ListHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.PerhitunganHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.PerhitunganPreviewHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.PilihHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.SeriusHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.SeriusPreviewHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.TanggalTempoScreen
import com.example.lunasin.Frontend.UI.Inputhutang.TemanHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.TemanPreviewHutangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.utang.ListUtangScreen
import com.example.lunasin.Frontend.UI.Inputhutang.utang.PreviewUtangScreen
import com.example.lunasin.Frontend.UI.Profile.ProfileScreen
import com.example.lunasin.Frontend.UI.Statistic.StatisticScreen
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticViewModel
import com.example.lunasin.Frontend.viewmodel.Statistic.StatisticViewModelFactory
import com.example.lunasin.ui.screens.ForgotPasswordScreen
import com.example.lunasin.ui.screens.LoginScreen
import com.example.lunasin.ui.screens.SignUpScreen
import com.example.lunasin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// DIUBAH: Menghapus parameter startDestination yang tidak terpakai
@Composable
fun NavGraph(authViewModel: AuthViewModel, hutangViewModel: HutangViewModel) {
    val navController = rememberNavController()

    // Menentukan tujuan awal berdasarkan status login
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "home_screen"
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination // Menggunakan tujuan awal yang sudah ditentukan
    ) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel, navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(authViewModel, navController)
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
            PilihHutangScreen(navController)
        }
        composable("list_hutang_screen") { ListHutangScreen(hutangViewModel, navController) }
        composable("home_screen") {
            HomeScreen(navController, hutangViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable("list_utang_screen") {
            ListUtangScreen(hutangViewModel, navController)
        }
        composable(
            route = "preview_utang/{docId}",
            arguments = listOf(navArgument("docId") { type = NavType.StringType })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PreviewUtangScreen(
                viewModel = hutangViewModel,
                navController = navController,
                docId = docId,
                userId = userId
            )
        }

        // --- RUTE STATISTIK YANG DIPERBAIKI ---
        composable(Screen.Statistic.route) {
            // Ambil application context agar aman
            val application = LocalContext.current.applicationContext as Application

            val statisticViewModel: StatisticViewModel = viewModel(
                factory = StatisticViewModelFactory(
                    // Menggunakan application context yang aman untuk Repository
                    profileRepository = ProfileRepository(application),
                    hutangRepository = HutangRepository(FirebaseFirestore.getInstance())
                )
            )
            StatisticScreen(
                navController = navController,
                statisticViewModel = statisticViewModel
            )
        }
    }
}