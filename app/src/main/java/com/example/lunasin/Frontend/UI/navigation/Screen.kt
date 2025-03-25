package com.example.lunasin.Frontend.UI.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object SignUp : Screen("signup_screen")
}