package com.example.lunasin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
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
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var hutangViewModel: HutangViewModel
    private lateinit var piutangViewModel: PiutangViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
            subscribeToTopic()
        } else {
            Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(authRepository))[AuthViewModel::class.java]
        val firestoreService = FirestoreService()
        hutangViewModel = ViewModelProvider(this, HutangViewModelFactory(firestoreService))[HutangViewModel::class.java]
        piutangViewModel = ViewModelProvider(this, PiutangViewModelFactory(firestoreService))[PiutangViewModel::class.java]

        askNotificationPermission()

        setContent {
            LunasinTheme {
                val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }
                NavGraph(authViewModel, hutangViewModel, piutangViewModel, startDestination)
            }
        }

        // Tangani intent dari notifikasi
        handleNotificationIntent()

        notifikasiHarian()
    }

    private fun notifikasiHarian() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotifikasiUtils>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_notification_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    subscribeToTopic()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            subscribeToTopic()
        }
    }

    private fun subscribeToTopic() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(userId)
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) {
                        "Berhasil subscribe ke topik $userId"
                    } else {
                        "Gagal subscribe: ${task.exception?.message}"
                    }
                    Log.d("MainActivity", msg)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.w("MainActivity", "User tidak login, tidak bisa subscribe ke topik")
        }
    }

    private fun handleNotificationIntent() {
        val hutangId = intent.getStringExtra("hutangId")
        if (hutangId != null) {
            Log.d("MainActivity", "Menerima hutangId dari notifikasi: $hutangId")
            // Navigasi dilakukan di dalam setContent melalui NavGraph
        }
    }
}