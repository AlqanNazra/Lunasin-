package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

object HutangCalculator {
    // Hitung denda berdasarkan bunga tahunan
    fun dendaBunga_Tahunan(sisahutang: Double, bunga: Double, telat: Int): Double {
        val perBunga = bunga / 100
        val dendaPerHari = perBunga / 365
        val denda = dendaPerHari * sisahutang
        return denda * telat
    }

    // Hitung denda berdasarkan bunga bulanan
    fun dendaBunga_Bulan(sisahutang: Double, bunga: Double, telat: Int): Double {
        val perBunga = bunga / 100
        val dendaPerBulan = perBunga / 12
        val denda = dendaPerBulan * sisahutang
        return denda * telat
    }

    // Denda tetap hanya mengembalikan nilai denda yang diinput
    fun dendaTetap(denda: Double): Double {
        return denda
    }

    // Hitung denda untuk cicilan
    fun denda_Cicilan(sisahutang: Double, bunga: Double, telat: Int): Double {
        val perBunga = bunga / 100
        val denda = perBunga * sisahutang
        return denda * telat
    }

    // Hitung jumlah hari keterlambatan berdasarkan tanggal jatuh tempo
    fun hitungKeterlambatan(tanggalJatuhTempo: String, tanggalSekarang: LocalDate): Int {
        return try {
            val possibleFormats = listOf("d/M/yyyy", "dd/MM/yyyy")
            val jatuhTempo = possibleFormats.firstNotNullOfOrNull { format ->
                try {
                    LocalDate.parse(tanggalJatuhTempo, DateTimeFormatter.ofPattern(format))
                } catch (e: Exception) {
                    null
                }
            } ?: throw IllegalArgumentException("Format tanggal tidak dikenali")

            if (tanggalSekarang.isAfter(jatuhTempo)) {
                Period.between(jatuhTempo, tanggalSekarang).days
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("TanggalError", "Gagal menghitung keterlambatan: $tanggalJatuhTempo", e)
            0
        }
    }

    // Hitung denda per angsuran untuk listTempo
    fun hitungDendaListTempo(
        listTempo: List<Map<String, Any>>,
        nominalPerAngsuran: Double,
        bunga: Double,
        tanggalSekarang: LocalDate
    ): Double {
        var totalDenda = 0.0
        val possibleFormats = listOf("d/M/yyyy", "dd/MM/yyyy")

        listTempo.forEach { tempo ->
            val tanggalTempo = tempo["tanggalTempo"] as? String ?: return@forEach
            val angsuranKe = tempo["angsuranKe"] as? Int ?: return@forEach

            val jatuhTempo = possibleFormats.firstNotNullOfOrNull { format ->
                try {
                    LocalDate.parse(tanggalTempo, DateTimeFormatter.ofPattern(format))
                } catch (e: Exception) {
                    null
                }
            } ?: return@forEach

            if (tanggalSekarang.isAfter(jatuhTempo)) {
                val telatHari = Period.between(jatuhTempo, tanggalSekarang).days
                val denda = denda_Cicilan(nominalPerAngsuran, bunga, telatHari)
                totalDenda += denda
                Log.d("DendaListTempo", "Angsuran ke-$angsuranKe terlambat $telatHari hari, denda: $denda")
            }
        }
        return totalDenda
    }

    // Personaliasi Akun
    fun Debt_to_Income(Totalpembayaran: Double, Pendapatan: Double): String {
        val dti = (Totalpembayaran / Pendapatan) * 100
        return if (dti >= 40) {
            "Warning: DTI Anda $dti% - Risiko tinggi!"
        } else {
            "Aman: DTI Anda $dti% - Keuangan stabil."
        }
    }

    fun debt_Service_Coverage_Ratio(pendapatan: Double, totalcicilan: Double): String {
        val dscr = totalcicilan / pendapatan
        return if (dscr >= 1) {
            "Aman: Risiko gagal bayar aman"
        } else {
            "Warning: Risiko gagal bayar tinggi"
        }
    }

    fun rumus_Amorsiasi(angsuranPerbulan: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaDesimal = bunga / 100
        val total1 = 1 + bungaDesimal
        val total2_pembagi = pangkat(total1, lamaPinjam) - 1
        val total1_penyebut = pangkat(total1, lamaPinjam)
        val total2_penyebut = total1_penyebut * bungaDesimal * angsuranPerbulan
        return total2_penyebut / total2_pembagi
    }

    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }

    fun pangkat(base: Double, exponent: Int): Double {
        var result = 1.0
        for (i in 1..exponent) {
            result *= base
        }
        return result
    }

    fun cicilanPerbulan(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaDesimal = bunga / 100.0
        val bungaR = 1 + bungaDesimal
        val total1 = nominalPinjaman * bungaDesimal * pangkat(bungaR, lamaPinjam)
        val total2 = pangkat(bungaR, lamaPinjam) - 1
        return if (total2 != 0.0) total1 / total2 else nominalPinjaman / lamaPinjam
    }

    fun hitungBunga(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaDesimal = bunga / 100.0
        return nominalPinjaman * bungaDesimal * lamaPinjam
    }

    fun hitungTanggalAkhir(tanggalPinjam: String, lamaPinjam: Int): String {
        return try {
            val possibleFormats = listOf("d/M/yyyy", "dd/MM/yyyy")
            val tanggalawal = possibleFormats.firstNotNullOfOrNull { format ->
                try {
                    LocalDate.parse(tanggalPinjam, DateTimeFormatter.ofPattern(format))
                } catch (e: Exception) {
                    null
                }
            } ?: throw IllegalArgumentException("Format tanggal tidak dikenali")
            val tanggalakhir = tanggalawal.plusMonths(lamaPinjam.toLong())
            tanggalakhir.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            Log.e("TanggalError", "Format tanggal salah: $tanggalPinjam", e)
            "Format Salah"
        }
    }

    fun hitungTotalHutang(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaTotal = hitungBunga(nominalPinjaman, bunga, lamaPinjam)
        return nominalPinjaman + bungaTotal
    }
}