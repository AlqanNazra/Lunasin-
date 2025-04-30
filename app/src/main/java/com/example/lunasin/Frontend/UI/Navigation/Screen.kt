package com.example.lunasin.Frontend.UI.Navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object SignUp : Screen("signup_screen")
    object ForgotScreen : Screen("forgot_password_screen")
    object ListHutangScreen : Screen("list_piutang_screen")
    object Profile : Screen("profile_screen") // Ditambahkan
    object Search : Screen("search_screen") // Ditambahkan untuk BottomNav
    object Stats : Screen("stats_screen") // Ditambahkan untuk BottomNav
}