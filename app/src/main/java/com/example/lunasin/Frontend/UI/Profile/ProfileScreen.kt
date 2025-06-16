package com.example.lunasin.Frontend.UI.Profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.lunasin.Frontend.UI.Home.AppBottomNavigationBar
import com.example.lunasin.Frontend.UI.navigation.Screen
import com.example.lunasin.Frontend.viewmodel.Profile.ProfileViewModel
import com.example.lunasin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    var isEditingPassword by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    val profilePictureUrl = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(user?.email) {
        user?.email?.let { email ->
            FirebaseStorage.getInstance().reference
                .child("profile_pictures/$email.jpg")
                .downloadUrl
                .addOnSuccessListener { uri -> profilePictureUrl.value = uri.toString() }
                .addOnFailureListener { Log.e("ProfileScreen", "load pic", it) }
        }
    }
    val painter = profilePictureUrl.value
        ?.let { rememberAsyncImagePainter(it) }
        ?: painterResource(R.drawable.blankprofile)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = { AppBottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painter,
                contentDescription = "Foto Profil",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = vm.name.ifEmpty { user?.displayName ?: "Nama Pengguna" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ProfileOptionItem(text = "Ubah Password") { isEditingPassword = true; isEditingProfile = false }
                    Divider()
                    ProfileOptionItem(text = "Edit Profil") { isEditingProfile = true; isEditingPassword = false }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (isEditingProfile) {
                EditProfileForm(vm = vm, onSave = { isEditingProfile = false })
            }

            if (isEditingPassword) {
                EditPasswordForm(
                    newPassword = newPassword,
                    onPasswordChange = { newPassword = it },
                    onSave = { isEditingPassword = false; newPassword = "" },
                    onCancel = { isEditingPassword = false; newPassword = "" }
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditProfileForm(vm: ProfileViewModel, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vm.name, onValueChange = vm::updateName, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vm.address, onValueChange = vm::updateAddress, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vm.phone, onValueChange = vm::updatePhone, label = { Text("Telepon") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = vm.incomeText, onValueChange = vm::updateIncome, label = { Text("Pendapatan / bulan") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
        Button(
            onClick = { vm.saveProfile(); onSave() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Profil")
        }
    }
}

@Composable
private fun EditPasswordForm(
    newPassword: String,
    onPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newPassword,
                onValueChange = onPasswordChange,
                label = { Text("Password Baru") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Batal") }
            Button(
                onClick = {
                    // --- INI ADALAH LOGIKA PERBAIKAN UNTUK MENCEGAH CRASH ---
                    if (newPassword.isNotBlank() && newPassword.length >= 6) {
                        user?.updatePassword(newPassword)
                            ?.addOnSuccessListener {
                                Toast.makeText(context, "Password berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                                onSave() // Menutup form jika berhasil
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Password tidak boleh kosong dan minimal 6 karakter.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Simpan")
            }
        }
    }
}