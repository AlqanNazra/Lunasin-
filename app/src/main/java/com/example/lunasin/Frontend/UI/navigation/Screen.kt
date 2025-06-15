package com.example.lunasin.Frontend.UI.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object SignUp : Screen("signup_screen")
    object ForgotScreen : Screen("forgot_password_screen")
    object ListHutangScreen : Screen("list_hutang_screen")
    object Profile : Screen("profile_screen")
    object Statistic : Screen("statistic_screen")

}