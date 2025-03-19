package com.example.lunasin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.lunasin.ui.screens.*
import com.example.lunasin.viewmodel.AuthViewModel

@Composable
fun NavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel, navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(authViewModel, navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(authViewModel, navController)
        }
    }
}