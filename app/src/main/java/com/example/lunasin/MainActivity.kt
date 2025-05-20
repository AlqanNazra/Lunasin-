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

    // Untuk FCM (Android 13+ permission)
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

        // Ubah status bar dan navigation bar jadi putih, dan ikon jadi gelap
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // Inisialisasi ViewModel
        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(authRepository))[AuthViewModel::class.java]

        val firestoreService = FirestoreService()
        hutangViewModel = ViewModelProvider(this, HutangViewModelFactory(firestoreService))[HutangViewModel::class.java]
        piutangViewModel = ViewModelProvider(this, PiutangViewModelFactory(firestoreService))[PiutangViewModel::class.java]

        // Minta izin notifikasi dan subscribe ke topik FCM
        askNotificationPermission()

        // Tampilkan konten
        setContent {
            LunasinTheme {
                val startDestination = Screen.Login.route
                NavGraph(authViewModel, hutangViewModel, piutangViewModel, startDestination)
            }
        }

        // Jalankan notifikasi harian
        notifikasiHarian()
    }

    // ✅ Fungsi notifikasi harian (local)
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

    // ✅ Fungsi minta izin notifikasi (Android 13+)
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Izin sudah diberikan
                    subscribeToTopic()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Bisa tampilkan penjelasan UI di sini
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Versi Android < 13 tidak perlu izin
            subscribeToTopic()
        }
    }

    // ✅ Fungsi subscribe ke topik FCM
    private fun subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("reminder_hutang")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "Berhasil subscribe ke reminder_hutang"
                } else {
                    "Gagal subscribe: ${task.exception?.message}"
                }
                Log.d("MainActivity", msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

}
