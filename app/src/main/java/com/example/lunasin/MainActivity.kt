package com.example.lunasin

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lunasin.Frontend.UI.navigation.NavGraph
import com.example.lunasin.Frontend.UI.navigation.Screen
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Frontend.viewmodel.Authentifikasi.AuthViewModelFactory
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModelFactory
import com.example.lunasin.theme.LunasinTheme
import com.example.lunasin.utils.NotifikasiUtils
import com.example.lunasin.utils.OnboardingPrefs
import com.example.lunasin.viewmodel.AuthViewModel
import android.Manifest
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

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

        // Minta izin notifikasi untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val onboardingCompleted = OnboardingPrefs.hasCompletedOnboarding(this)
        println("Status Onboarding: $onboardingCompleted")

        OnboardingPrefs.resetOnboarding(this)

        setContent {
            LunasinTheme {
                // Tentukan tujuan awal berdasarkan status onboarding
                val startDestination = if (OnboardingPrefs.hasCompletedOnboarding(this)) {
                    Screen.Login.route
                } else {
                    Screen.Onboarding.route
                }
                println("Start destination: $startDestination")
                NavGraph(authViewModel, hutangViewModel, startDestination = startDestination)
            }
        }

        notifikasiHarian()
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