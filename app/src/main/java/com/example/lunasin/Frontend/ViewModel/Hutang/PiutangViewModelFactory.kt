package com.example.lunasin.Frontend.ViewModel.Hutang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Frontend.ViewModel.Hutang.PiutangViewModel

class PiutangViewModelFactory(private val firestoreService: FirestoreService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PiutangViewModel::class.java)) {
            return PiutangViewModel(firestoreService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}