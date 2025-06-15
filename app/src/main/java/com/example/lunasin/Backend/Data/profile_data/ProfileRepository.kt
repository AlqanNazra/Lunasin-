package com.example.lunasin.Backend.Data.profile_data

import android.content.Context
import com.example.lunasin.Backend.model.Profile

class ProfileRepository(context: Context) {
    private val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    suspend fun getProfile(): Profile {
        val name   = prefs.getString("name", "") ?: ""
        val addr   = prefs.getString("address", "") ?: ""
        val phone  = prefs.getString("phone", "") ?: ""
        val income = prefs.getString("income", "0.0")!!.toDouble()
        return Profile(name, addr, phone, income)
    }

    fun saveProfile(p: Profile) {
        prefs.edit()
            .putString("name", p.name)
            .putString("address", p.address)
            .putString("phone", p.phone)
            .putString("income", p.monthlyIncome.toString())
            .apply()
    }
}
