package com.example.lunasin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.example.lunasin.utils.NotifikasiUtils
import com.example.lunasin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var hutangViewModel: HutangViewModel
    private lateinit var piutangViewModel: PiutangViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ubah status bar dan navigation bar jadi putih, dan ikon jadi gelap
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

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

        // Minta izin notifikasi untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            LunasinTheme {
                val startDestination = Screen.Login.route
                NavGraph(authViewModel, hutangViewModel, piutangViewModel, startDestination)
            }
            notifikasiHarian()
        }
    }

    private fun notifikasiHarian() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotifikasiUtils>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_notification_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
