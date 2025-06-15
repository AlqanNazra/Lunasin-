package com.example.lunasin.Backend.Model

data class Profile(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val monthlyIncome: Double = 0.0,
    val debtLimit: Double = 0.0 // Tambahkan debtLimit ke model
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "address" to address,
            "phone" to phone,
            "monthlyIncome" to monthlyIncome,
            "debtLimit" to debtLimit
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Profile {
            return Profile(
                name = map["name"] as? String ?: "",
                address = map["address"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                monthlyIncome = (map["monthlyIncome"] as? Number)?.toDouble() ?: 0.0,
                debtLimit = (map["debtLimit"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}