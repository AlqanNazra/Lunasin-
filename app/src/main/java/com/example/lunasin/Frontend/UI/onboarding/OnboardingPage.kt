package com.example.lunasin.Frontend.UI.onboarding

import com.example.lunasin.R

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageResId: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Selamat Datang",
        description = "Aplikasi Lunasin membantu kamu mengelola hutang dengan mudah.",
        imageResId = R.drawable.ic_onboarding_1 // Ganti dengan drawable Anda
    ),
    OnboardingPage(
        title = "Pantau Pembayaran",
        description = "Catat angsuran, tenggat waktu, dan progres pembayaran.",
        imageResId = R.drawable.ic_onboarding_2
    ),
    OnboardingPage(
        title = "Dapatkan Notifikasi",
        description = "Terima pengingat sebelum jatuh tempo agar tidak lupa bayar.",
        imageResId = R.drawable.ic_onboarding_3
    )
)
