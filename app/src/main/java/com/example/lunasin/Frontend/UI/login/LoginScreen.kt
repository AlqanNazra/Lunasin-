package com.example.lunasin.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lunasin.R
import com.example.lunasin.navigation.Screen
import com.example.lunasin.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.lunasin.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                authViewModel.signInWithGoogle(token)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Biarkan tinggi adaptif (85% layar)
                .align(Alignment.BottomCenter),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Bisa di-scroll jika kontennya panjang
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hello There Welcome To Lunasin",
                    style = MaterialTheme.typography.labelSmall,
                    color = Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Lock Icon",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Tambahkan padding agar tidak mentok ke sisi layar
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp), // Membuat sudut membulat
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface, // Background saat fokus
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface, // Background saat tidak fokus
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary, // Warna border saat aktif
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Warna border saat tidak aktif
                        cursorColor = MaterialTheme.colorScheme.primary // Warna kursor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Tambahkan padding agar tidak mentok ke sisi layar
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp), // Membuat sudut membulat
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface, // Background saat fokus
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface, // Background saat tidak fokus
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary, // Warna border saat aktif
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Warna border saat tidak aktif
                        cursorColor = MaterialTheme.colorScheme.primary // Warna kursor
                    )
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { navController.navigate(Screen.ForgotScreen.route) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.labelSmall,
                            color = HeaderColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { authViewModel.signIn(email, password, navController) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val googleSignInClient = GoogleSignIn.getClient(
                    context,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("705647658689-u4vdhpllbf5j0so3p7r3kub5jg0raf2m.apps.googleusercontent.com")
                        .requestEmail()
                        .build()
                )

                Button(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Sign in with Google", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val annotatedText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Black)) {
                            append("Don't have an account? ")
                        }
                        pushStringAnnotation(tag = "SignUp", annotation = "SignUp")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                            append("Sign Up")
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.labelSmall,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "SignUp", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    navController.navigate(Screen.SignUp.route)
                                }
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Toast.makeText(context, "Login berhasil", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.ListHutangScreen.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
}

















