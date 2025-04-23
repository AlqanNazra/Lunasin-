package com.example.lunasin.Backend.model




data class Tempo(
    val angsuranKe: Int = 0,
    val tanggalTempo: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "angsuranKe" to angsuranKe,
            "tanggalTempo" to tanggalTempo
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Tempo {
            return Tempo(
                angsuranKe = (map["angsuranKe"] as? Long)?.toInt() ?: 0,
                tanggalTempo = map["tanggalTempo"] as? String ?: ""
            )
        }
    }
}

data class Hutang(
    // model Default untuk model hutang yang ada
    val docId: String = "",
    val userId: String = "",
    val namapinjaman: String = "",
    val nominalpinjaman: Double = 0.0,
    val tanggalPinjam: String = "",
    val catatan: String = "",
    var id_penerima: String? = null, // null jika belum diklaim
    //Model untuk perhitungan hutang
    val totalHutang: Double = 0.0,
    val totalDenda: Double = 0.0,
    val tanggalBayar: String = "",
    //Model untuk perhitungan hutang
    val listTempo: List<Tempo> = emptyList(),
    val bunga: Double = 0.0,
    val lamaPinjaman: Int = 0,
    val totalbunga: Double = 0.0,
    val totalcicilan: Double = 0.0,
) {
    fun toMap(): Map<String, Any?> { // id_penerima bisa null, jadi pakai Any?
        return mapOf(
            // model Default untuk model hutang yang ada
            "docId" to docId,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "tanggalPinjam" to tanggalPinjam,
            "catatan" to catatan,
            "id_penerima" to id_penerima,
            // Model Perhitungan Hutang
            "totalHutang" to totalHutang,
            "totalDenda" to totalDenda,
            "tanggalBayar" to tanggalBayar,
            // Model Perhitungan Serius
            "bunga" to bunga,
            "lamaPinjaman" to lamaPinjaman,
            "totalcicilan" to totalcicilan,
            "totalbunga" to totalbunga,
            "listTempo" to listTempo.map { it.toMap() } // Konversi list ke List<Map<String, Any>>
             // Tambahkan ini agar bisa disimpan

        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Hutang {
            return Hutang(
                docId = map["docId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                namapinjaman = map["namapinjaman"] as? String ?: "",
                nominalpinjaman = when (val value = map["nominalpinjaman"]) {
                    is Number -> value.toDouble()
                    else -> 0.0
                },
                bunga = (map["bunga"] as? Double) ?: 0.0,
                lamaPinjaman = (map["lamaPinjaman"] as? Long)?.toInt() ?: 0,
                totalHutang = (map["totalHutang"] as? Double) ?: 0.0,
                tanggalPinjam = map["tanggalPinjam"] as? String ?: "",
                tanggalBayar = map["tanggalBayar"] as? String ?: "",
                totalcicilan = (map["totalcicilan"] as? Double) ?: 0.0,
                totalbunga = (map["totalbunga"] as? Double) ?: 0.0,
                catatan = map["catatan"] as? String ?: "",
                id_penerima = map["id_penerima"] as? String, // Ambil dari Firestore
                listTempo = (map["listTempo"] as? List<*>)?.mapNotNull {
                    (it as? Map<String, Any>)?.let { Tempo.fromMap(it) }
                } ?: emptyList()

            )
        }
    }
}