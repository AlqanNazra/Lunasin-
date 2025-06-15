package com.example.lunasin.Frontend.ViewModel.Profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Data.profile_data.ProfileRepository
import com.example.lunasin.Backend.Model.Profile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    var name by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var incomeText by mutableStateOf("")
        private set
    var debtLimit by mutableStateOf(0.0)
        private set
    var isProfileSaved by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("ProfileViewModel", "Pengguna tidak login")
            errorMessage = "Harap login terlebih dahulu"
            return
        }

        viewModelScope.launch {
            try {
                val profile = repository.getProfile(user.uid)
                if (profile.name.isBlank()) {
                    name = user.displayName ?: "Nama Pengguna"
                    address = ""
                    phone = ""
                    incomeText = "0"
                    debtLimit = 0.0
                    isProfileSaved = false
                } else {
                    name = profile.name
                    address = profile.address
                    phone = profile.phone
                    incomeText = profile.monthlyIncome.toString()
                    debtLimit = profile.debtLimit
                    isProfileSaved = true
                }
                Log.d("ProfileViewModel", "Profil dimuat: $profile")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal memuat profil: ${e.message}", e)
                errorMessage = "Gagal memuat profil"
            }
        }
    }

    private fun calculateDebtLimit(income: Double): Double {
        return income * 0.5
    }

    fun updateName(newName: String) { name = newName }
    fun updateAddress(newAddress: String) { address = newAddress }
    fun updatePhone(newPhone: String) { phone = newPhone }
    fun updateIncome(newIncome: String) {
        incomeText = newIncome
        val income = newIncome.toDoubleOrNull() ?: 0.0
        debtLimit = calculateDebtLimit(income)
    }

    fun saveProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("ProfileViewModel", "Pengguna tidak login")
            errorMessage = "Harap login terlebih dahulu"
            return
        }

        val income = incomeText.toDoubleOrNull() ?: 0.0
        val profile = Profile(
            name = name,
            address = address,
            phone = phone,
            monthlyIncome = income,
            debtLimit = debtLimit
        )

        viewModelScope.launch {
            try {
                repository.saveProfile(user.uid, profile)
                isProfileSaved = true
                errorMessage = null
                Log.d("ProfileViewModel", "Profil berhasil disimpan: $profile")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal menyimpan profil: ${e.message}", e)
                errorMessage = "Gagal menyimpan profil"
            }
        }
    }

    fun editProfile() {
        isProfileSaved = false
    }
}