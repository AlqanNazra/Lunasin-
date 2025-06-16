package com.example.lunasin.Frontend.viewmodel.Profile

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.lunasin.Backend.Data.profile_data.ProfileRepository
import com.example.lunasin.Backend.model.Profile
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProfileRepository(app)

    var name by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var incomeText by mutableStateOf("")
        private set
    var debtLimit by mutableStateOf(0.0)  // Properti untuk limit hutang
        private set

    var isProfileSaved by mutableStateOf(false)
        private set

    init {
        // Langsung gunakan viewModelScope yang sudah disediakan secara otomatis.
        // Tidak perlu dibuat lagi.
        viewModelScope.launch {
            loadProfile()
        }
    }

    private suspend fun loadProfile() {
        Log.d("ProfileViewModel", "Loading profile...")
        val p: Profile = repo.getProfile()
        Log.d("ProfileViewModel", "Loaded Profile: $p")
        if (p.name.isBlank()) {
            val user = FirebaseAuth.getInstance().currentUser
            name = user?.displayName ?: "Nama Pengguna"
            address = ""
            phone = ""
            incomeText = "0"
            debtLimit = 0.0
        } else {
            name = p.name
            address = p.address
            phone = p.phone
            incomeText = p.monthlyIncome.toString()
            debtLimit = calculateDebtLimit(p.monthlyIncome)
        }
        isProfileSaved = p.name.isNotBlank()
    }

    // Menghitung limit hutang sebagai 50% dari pendapatan
    private fun calculateDebtLimit(income: Double): Double {
        return income * 0.5
    }

    fun updateName(newName: String)     { name = newName }
    fun updateAddress(newAddress: String) { address = newAddress }
    fun updatePhone(newPhone: String)    { phone = newPhone }
    fun updateIncome(newIncome: String)  {
        incomeText = newIncome
        val income = newIncome.toDoubleOrNull() ?: 0.0
        debtLimit = calculateDebtLimit(income)
    }

    fun saveProfile() {
        val income = incomeText.toDoubleOrNull() ?: 0.0
        val profile = Profile(name, address, phone, income)
        repo.saveProfile(profile)
        debtLimit = calculateDebtLimit(income)  // Hitung ulang limit hutang saat menyimpan
        isProfileSaved = true
    }

    fun editProfile() {
        isProfileSaved = false
    }
}
