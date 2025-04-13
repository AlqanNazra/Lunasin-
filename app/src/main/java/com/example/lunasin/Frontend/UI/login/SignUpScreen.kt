package com.example.lunasin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.R
import com.example.lunasin.theme.Black
import com.example.lunasin.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(authViewModel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val resendTimer by authViewModel.resendTimer.collectAsState()
    var showResendOption by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            // Header dengan back arrow dan judul
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate("login_screen") }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .align(Alignment.BottomCenter),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Welcome To Us",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hello there, create New account",
                        style = MaterialTheme.typography.labelSmall,
                        color = Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_signup),
                            contentDescription = "Signup Icon",
                            modifier = Modifier
                                .size(150.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LaunchedEffect(errorMessage) {
                        errorMessage?.let { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                                authViewModel.clearErrorMessage() // <-- kita buat ini di langkah 2
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Semua field harus diisi!")
                                    }
                                }
                                password != confirmPassword -> {
                                    authViewModel.setError("Passwords do not match")
                                }
                                else -> {
                                    authViewModel.signUp(email, password, navController)
                                    showResendOption = true
                                    authViewModel.startResendTimer()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Email verifikasi dikirim. Silakan cek inbox.")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Sign Up")
                    }

                    if (showResendOption) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (resendTimer > 0) {
                            Text(
                                text = "Kirim ulang email verifikasi dalam ${resendTimer}s",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    authViewModel.resendVerificationEmail(context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Email verifikasi dikirim ulang.")
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Kirim ulang email verifikasi")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        val annotatedText = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Black)) {
                                append("Already have an account? ")
                            }
                            pushStringAnnotation(tag = "Login", annotation = "Login")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Login")
                            }
                            pop()
                        }

                        ClickableText(
                            text = annotatedText,
                            style = MaterialTheme.typography.labelSmall,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations("Login", offset, offset)
                                    .firstOrNull()?.let {
                                        navController.navigate("login_screen")
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

