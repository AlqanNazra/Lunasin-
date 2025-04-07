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
    val docId: String = "",
    val userId: String = "",
    val namapinjaman: String = "",
    val nominalpinjaman: Double = 0.0,
    val bunga: Double = 0.0,
    val lamaPinjaman: Int = 0,
    val totalHutang: Double = 0.0,
    val tanggalPinjam: String = "",
    val tanggalBayar: String = "",
    val totalcicilan: Double = 0.0,
    val totalbunga: Double = 0.0,
    val catatan: String = "",
    var id_penerima: String? = null, // Bisa null jika belum diklaim
    val listTempo: List<Tempo> = emptyList(),
) {
    fun toMap(): Map<String, Any?> { // id_penerima bisa null, jadi pakai Any?
        return mapOf(
            "docId" to docId,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "bunga" to bunga,
            "lamaPinjaman" to lamaPinjaman,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalBayar" to tanggalBayar,
            "totalcicilan" to totalcicilan,
            "totalbunga" to totalbunga,
            "catatan" to catatan,
            "id_penerima" to id_penerima, // Tambahkan ini agar bisa disimpan
            "listTempo" to listTempo.map { it.toMap() } // Konversi list ke List<Map<String, Any>>
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Hutang {
            return Hutang(
                docId = map["docId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                namapinjaman = map["namapinjaman"] as? String ?: "",
                nominalpinjaman = (map["nominalpinjaman"] as? Double) ?: 0.0,
                bunga = (map["bunga"] as? Double) ?: 0.0,
                lamaPinjaman = (map["lamaPinjaman"] as? Long)?.toInt() ?: 0,
                totalHutang = (map["totalHutang"] as? Double) ?: 0.0,
                tanggalPinjam = map["tanggalPinjam"] as? String ?: "",
                tanggalBayar = map["tanggalBayar"] as? String ?: "",
                totalcicilan = (map["totalcicilan"] as? Double) ?: 0.0,
                totalbunga = (map["totalbunga"] as? Double) ?: 0.0,
                catatan = map["catatan"] as? String ?: "",
                id_penerima = map["id_penerima"] as? String, // Ambil dari Firestore
                listTempo = (map["listTempo"] as? List<Map<String, Any>>)?.map { Tempo.fromMap(it) } ?: emptyList()
            )
        }
    }
}






