package com.example.lunasin.Backend.Model

// Definisikan enum HutangType di sini
enum class HutangType {
    TEMAN,
    PERHITUNGAN
}

// Definisikan enum StatusBayar untuk status pembayaran
enum class StatusBayar {
    LUNAS,
    BELUM_LUNAS
}

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
    val Id_Transaksi: String = "",
    val docId: String = "",
    val userId: String = "",
    val namapinjaman: String = "",
    val nominalpinjaman: Double = 0.0,
    val tanggalPinjam: String = "",
    val catatan: String = "",
    var id_penerima: String? = null, // null jika belum diklaim
    // Model untuk perhitungan hutang
    val totalHutang: Double = 0.0,
    val totalDenda: Double = 0.0,
    val tanggalBayar: String = "", // Untuk menyimpan tanggal pembayaran (jika sudah dibayar)
    // Model untuk perhitungan hutang
    val listTempo: List<Tempo> = emptyList(),
    val bunga: Double = 0.0,
    val lamaPinjaman: Int = 0,
    val totalbunga: Double = 0.0,
    val totalcicilan: Double = 0.0,
    // Tambahkan field untuk jenis hutang
    val hutangType: HutangType? = null,
    // Tambahkan field untuk membedakan Hutang/Piutang
    val type: String = "",
    // Tambahkan field untuk tanggal jatuh tempo
    val tanggalJatuhTempo: String = "",
    // Tambahkan field untuk status pembayaran
    val statusBayar: StatusBayar? = StatusBayar.BELUM_LUNAS, // Default ke BELUM_LUNAS
    val buktiPembayaranUrl: String? = null,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            // model Default untuk model hutang yang ada
            "docId" to docId,
            "userId" to userId,
            "Id_Transaksi" to Id_Transaksi,
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
            "listTempo" to listTempo.map { it.toMap() }, // Konversi list ke List<Map<String, Any>>
            // Simpan hutangType sebagai String
            "hutangType" to hutangType?.name,
            // Simpan type
            "type" to type,
            // Simpan tanggal jatuh tempo
            "tanggalJatuhTempo" to tanggalJatuhTempo,
            // Simpan statusBayar sebagai String
            "statusBayar" to statusBayar?.name,
            "buktiPembayaranUrl" to buktiPembayaranUrl,
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Hutang {
            return Hutang(
                // model Default untuk model hutang yang ada
                Id_Transaksi = map["Id_Transaksi"] as? String ?: "",
                docId = map["docId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                namapinjaman = map["namapinjaman"] as? String ?: "",
                nominalpinjaman = when (val value = map["nominalpinjaman"]) {
                    is Number -> value.toDouble()
                    else -> 0.0
                },
                totalDenda = (map["totalDenda"] as? Number)?.toDouble() ?: 0.0,
                bunga = (map["bunga"] as? Double) ?: 0.0,
                lamaPinjaman = (map["lamaPinjaman"] as? Long)?.toInt() ?: 0,
                totalHutang = (map["totalHutang"] as? Double) ?: 0.0,
                tanggalPinjam = map["tanggalPinjam"] as? String ?: "",
                tanggalBayar = map["tanggalBayar"] as? String ?: "",
                totalcicilan = (map["totalcicilan"] as? Double) ?: 0.0,
                totalbunga = (map["totalbunga"] as? Double) ?: 0.0,
                catatan = map["catatan"] as? String ?: "",
                id_penerima = map["id_penerima"] as? String,
                listTempo = (map["listTempo"] as? List<*>)?.mapNotNull {
                    (it as? Map<String, Any>)?.let { Tempo.fromMap(it) }
                } ?: emptyList(),
                // Konversi String dari Firestore kembali ke HutangType
                hutangType = try {
                    map["hutangType"]?.let { HutangType.valueOf(it as String) }
                } catch (e: Exception) {
                    null
                },
                // Ambil type dari Firestore
                type = map["type"] as? String ?: "",
                // Ambil tanggal jatuh tempo dari Firestore
                tanggalJatuhTempo = map["tanggalJatuhTempo"] as? String ?: "",
                buktiPembayaranUrl = map["buktiPembayaranUrl"] as? String,
                // Konversi String dari Firestore kembali ke StatusBayar
                statusBayar = try {
                    map["statusBayar"]?.let { StatusBayar.valueOf(it as String) }
                } catch (e: Exception) {
                    StatusBayar.BELUM_LUNAS // Default ke BELUM_LUNAS jika gagal
                }

            )
        }
    }
}