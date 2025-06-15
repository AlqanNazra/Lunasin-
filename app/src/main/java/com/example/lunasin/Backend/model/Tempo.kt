package com.example.lunasin.Backend.model

import java.util.Date // Penting: Gunakan Date untuk kompatibilitas Timestamp Firestore

data class Tempo(
    val angsuranKe: Int = 0,
    val tanggalTempo: Date? = null,      // Tanggal jatuh tempo angsuran (sekarang Date)
    val amount: Double = 0.0,            // Jumlah angsuran
    val paid: Boolean = false,           // Status pembayaran
    val paymentDate: Date? = null        // Tanggal pembayaran (null jika belum dibayar) (sekarang Date)
)