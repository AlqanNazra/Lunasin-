package com.example.lunasin.Frontend.UI.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.Frontend.viewmodel.Authentifikasi.AuthViewModel

@Composable
fun SignUpScreen(authViewModel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val errorMessage by authViewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") })
        Spacer(modifier = Modifier.height(8.dp))

        if (!errorMessage.isNullOrEmpty()) {
            Text(text = errorMessage ?: "", color = Color.Red)
        }

        Button(onClick = {
            if (password == confirmPassword) {
                authViewModel.signUp(email, password, navController)
            } else {
                authViewModel.setError("Passwords do not match")
            }
        }) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login_screen") }) {
            Text("Already have an account? Login")
        }
    }
}
