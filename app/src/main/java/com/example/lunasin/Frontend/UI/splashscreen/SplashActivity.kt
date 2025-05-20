package com.example.lunasin.Frontend.UI.splashscreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.lunasin.MainActivity
import com.example.lunasin.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        splashScreen.setKeepOnScreenCondition { false } // Hapus splash setelah selesai

        // Delay (opsional) untuk simulasi loading
        Thread.sleep(2000) // 2 detik, sesuaikan kebutuhan
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}