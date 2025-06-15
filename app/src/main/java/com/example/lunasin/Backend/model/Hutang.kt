package com.example.lunasin.Backend.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date // Pastikan ini diimpor

data class Hutang(
    @DocumentId val docId: String = "",
    val userId: String = "",
    val namapinjaman: String = "",
    val nominalpinjaman: Double = 0.0,
    val bunga: Double = 0.0,
    val lamaPinjaman: Int = 0,
    val totalHutang: Double = 0.0,
    val tanggalPinjam: Date? = null, // UBAH ke Date?
    val tanggalBayar: Date? = null, // UBAH ke Date? (Jika ini tanggal pelunasan total hutang)
    val totalcicilan: Double = 0.0,
    val totalbunga: Double = 0.0,
    val catatan: String = "",
    var id_penerima: String? = null,
    val listTempo: List<Tempo> = emptyList(), // PASTIKAN TIPE DATANYA List<Tempo>
    @ServerTimestamp val createdAt: Date? = null
)