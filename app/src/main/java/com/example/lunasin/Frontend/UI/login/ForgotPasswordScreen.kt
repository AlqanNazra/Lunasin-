package com.example.lunasin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    resetPassword: (String, (Boolean, String?) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White // Semua latar putih
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .background(Color.White)
        ) {
            // Bagian atas: Icon Back dan Judul
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Forgot Password",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black
                )
            }

            // Keterangan kecil di atas TextField
            Text(
                text = "Input your email",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            val isButtonEnabled = email.text.isNotBlank() && !isLoading

            Button(
                onClick = {
                    isLoading = true
                    resetPassword(email.text) { success, message ->
                        isLoading = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message ?: "Unknown error")
                        }
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.LightGray,
                    contentColor = if (isButtonEnabled)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        Color.DarkGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Reset Password")
                }
            }
        }
        }
    }



