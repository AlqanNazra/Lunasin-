package com.example.lunasin.Frontend.UI.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.lunasin.Frontend.UI.Navigation.Screen
import com.example.lunasin.R
import com.example.lunasin.viewmodel.AuthViewModel
import com.example.lunasin.Frontend.viewmodel.Profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    var isEditingPassword by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    val profilePictureUrl = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(user?.email) {
        user?.email?.let { email ->
            FirebaseStorage.getInstance().reference
                .child("profile_pictures/$email.jpg")
                .downloadUrl
                .addOnSuccessListener { uri -> profilePictureUrl.value = uri.toString() }
                .addOnFailureListener { Log.e("ProfileScreen", "Failed to load profile picture", it) }
        }
    }

    val painter = profilePictureUrl.value
        ?.let { rememberAsyncImagePainter(it) }
        ?: painterResource(R.drawable.blankprofile)

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
            authViewModel.setAuthenticationState(false)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Logout berhasil")
            }
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
        containerColor = Color(0xFF80CBC4)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pading)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Profile Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )
            }

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profileViewModel.name.ifEmpty { user?.displayName ?: "Nama Pengguna" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Text(
                text = user?.email ?: "Tidak ada email",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingPassword = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Change Password", fontSize = 16.sp, color = Color.Black)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
                Divider(color = Color.LightGray, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingProfile = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Profile", fontSize = 16.sp, color = Color.Black)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nama: ${profileViewModel.name.ifEmpty { user?.displayName ?: "Nama Pengguna" }}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Alamat: ${profileViewModel.address}", fontSize = 16.sp)
                    Text("Telepon: ${profileViewModel.phone}", fontSize = 16.sp)
                    Text("Pendapatan: ${profileViewModel.incomeText}", fontSize = 16.sp)
                    Text("Limit Hutang: ${profileViewModel.debtLimit}", fontSize = 16.sp)
                }
            }

            if (isEditingProfile) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = profileViewModel.name,
                            onValueChange = { profileViewModel.updateName(it) },
                            label = { Text("Nama") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = profileViewModel.address,
                            onValueChange = { profileViewModel.updateAddress(it) },
                            label = { Text("Alamat") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = profileViewModel.phone,
                            onValueChange = { profileViewModel.updatePhone(it) },
                            label = { Text("Telepon") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = profileViewModel.incomeText,
                            onValueChange = { profileViewModel.updateIncome(it) },
                            label = { Text("Pendapatan / bulan") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        profileViewModel.saveProfile()
                        isEditingProfile = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Profil berhasil disimpan")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Simpan Profil", color = Color.White)
                }
            }

            if (isEditingPassword) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Save", color = Color.White)
                    }
                    Button(
                        onClick = {
                            isEditingPassword = false
                            newPassword = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { signOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: Int, val route: String)