package com.example.lunasin.Backend.model

data class Tempo(
    val angsuranKe: Int = 0,
    val tanggalTempo: String = ""
)

data class Hutang(
    val docId: String = "",
    val namapinjaman: String = "",
    val nominalpinjaman: Double = 0.0,
    val bunga: Double = 0.0,
    val lamaPinjaman: Int = 0,
    val tanggalPinjam: String = "",
    val tanggalBayar: String = "",
    val totalHutang: Double = 0.0,
    val gaji: Double = 0.0,
    val totalbunga: Double = 0.0,
    val totalcicilan: Double = 0.0,
    val periode: Int = 0,

    val listTempo: List<Tempo> = emptyList()
)


