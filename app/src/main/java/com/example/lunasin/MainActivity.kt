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
import com.example.lunasin.Backend.Data.login_Data.AuthRepository
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Frontend.UI.navigation.Screen
import com.example.lunasin.Frontend.viewmodel.Authentifikasi.AuthViewModelFactory
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModel
import com.example.lunasin.Frontend.viewmodel.Hutang.HutangViewModelFactory
import com.example.lunasin.theme.LunasinTheme
import com.example.lunasin.viewmodel.AuthViewModel
import android.Manifest
import com.example.lunasin.utils.NotifikasiUtils
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var hutangViewModel: HutangViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        val authFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        // Initialize FirestoreService
        val firestoreService = FirestoreService()
        val hutangFactory = HutangViewModelFactory(firestoreService)
        hutangViewModel = ViewModelProvider(this, hutangFactory)[HutangViewModel::class.java]

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            LunasinTheme {
                NavGraph(authViewModel, hutangViewModel, startDestination = Screen.Login.route)
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