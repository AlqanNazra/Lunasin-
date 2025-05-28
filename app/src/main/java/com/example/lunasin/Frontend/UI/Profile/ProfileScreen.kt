package com.example.lunasin.Frontend.UI.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.lunasin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.example.lunasin.Frontend.UI.Navigation.Screen
import com.example.lunasin.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
    var showDropdown by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var isEditingPassword by remember { mutableStateOf(false) }

    val profilePictureUrl = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(user?.email) {
        user?.email?.let { email ->
            val storageRef = FirebaseStorage.getInstance().reference
            val profilePicRef = storageRef.child("profile_pictures/$email.jpg")
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                profilePictureUrl.value = uri.toString()
            }.addOnFailureListener {
                Log.e("ProfileScreen", "Failed to load profile picture", it)
            }
        }
    }

    val painter = if (profilePictureUrl.value != null) {
        rememberAsyncImagePainter(profilePictureUrl.value)
    } else {
        painterResource(id = R.drawable.blankprofile)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun signOut(navController: NavHostController, authViewModel: AuthViewModel) {
        try {
            FirebaseAuth.getInstance().signOut()
            authViewModel.setAuthenticationState(false)  // Update state di AuthViewModel

            // Tampilkan Snackbar sebelum navigasi
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Logout berhasil")
            }

            // Navigasi dengan menghapus semua stack dan memastikan ke login
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error during signOut", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Gagal logout: ${e.message}")
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.primary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .background(MaterialTheme.colorScheme.primary),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Box {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Password") },
                            onClick = {
                                showDropdown = false
                                isEditingPassword = true
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user?.displayName ?: "Nama Pengguna",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = user?.email ?: "Tidak ada email",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (isEditingPassword) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password Icon"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("ProfileScreen", "Password updated successfully")
                                            isEditingPassword = false
                                            newPassword = ""
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Password berhasil diubah")
                                            }
                                        } else {
                                            Log.e("ProfileScreen", "Failed to update password", task.exception)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Gagal mengubah password")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }
                            Button(
                                onClick = {
                                    isEditingPassword = false
                                    newPassword = ""
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4444)
                    ),
                    modifier = Modifier
                        .clickable {
                            signOut(navController, authViewModel)
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Home", R.drawable.ic_home, "home_screen"),
            BottomNavItem("Search", R.drawable.ic_search, "search_screen"),
            BottomNavItem("Stats", R.drawable.ic_chart, "stats_screen"),
            BottomNavItem("Profile", R.drawable.ic_profile, "profile_screen")
        )

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            BottomNavigationItem(
                icon = {
                    Box(
                        modifier = if (isSelected) {
                            Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(6.dp)
                        } else {
                            Modifier.size(24.dp)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: Int, val route: String)