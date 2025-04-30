package com.example.lunasin.Frontend.ViewModel.Hutang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lunasin.Backend.Service.management_BE.FirestoreService

class HutangViewModelFactory(private val firestoreService: FirestoreService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HutangViewModel::class.java)) {
            return HutangViewModel(firestoreService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
