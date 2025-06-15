package com.example.lunasin.Frontend.ViewModel.Hutang

import android.util.Log
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Backend.Model.StatusBayar
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.Locale

object HutangCalculator {
    // Formatter untuk parsing tanggal dengan format fleksibel (d/M/yyyy atau dd/MM/yyyy)
    private val formatter = DateTimeFormatterBuilder()
        .appendPattern("d/M/yyyy")
        .optionalStart()
        .appendPattern("d/MM/yyyy")
        .optionalEnd()
        .toFormatter()

    // Hitung denda berdasarkan bunga bulanan
    fun dendaBunga_Bulan(sisahutang: Double, bunga: Double, telat: Int): Double {
        val perBunga = bunga / 100
        val dendaPerBulan = perBunga / 12
        val denda = dendaPerBulan * sisahutang
        return denda * telat
    }

    /**
     * Menghitung jumlah hari keterlambatan antara tanggal jatuh tempo dan tanggal sekarang.
     * @param tanggalJatuhTempo Tanggal jatuh tempo dalam format d/M/yyyy atau dd/MM/yyyy
     * @param tanggalSekarang Tanggal saat ini (default: LocalDate.now())
     * @return Jumlah hari keterlambatan (positif jika terlambat, negatif jika belum jatuh tempo, 0 jika sama atau error)
     */
    fun hitungKeterlambatan(tanggalJatuhTempo: String, tanggalSekarang: LocalDate = LocalDate.now()): Long {
        if (tanggalJatuhTempo.isBlank()) {
            Log.e("HutangCalculator", "Tanggal jatuh tempo kosong")
            return 0L
        }
        return try {
            val jatuhTempo = LocalDate.parse(tanggalJatuhTempo, formatter)
            val keterlambatan = ChronoUnit.DAYS.between(jatuhTempo, tanggalSekarang)
            Log.d("HutangCalculator", "Keterlambatan: $keterlambatan hari (jatuhTempo=$tanggalJatuhTempo, sekarang=$tanggalSekarang)")
            keterlambatan
        } catch (e: Exception) {
            Log.e("HutangCalculator", "Gagal parse tanggalJatuhTempo: $tanggalJatuhTempo", e)
            0L
        }
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

    fun hitungBunga(nominalPinjaman: Double, bunga: Double, lamaPinjam: Int): Double {
        val bungaDesimal = bunga / 100.0
        return nominalPinjaman * bungaDesimal * lamaPinjam
    }

    fun hitungTotalHutang(hutang: Hutang, tanggalSekarang: LocalDate = LocalDate.now()): Double {
        return when (hutang.hutangType) {
            HutangType.PERHITUNGAN -> {
                if (hutang.statusBayar == StatusBayar.BELUM_LUNAS) {
                    val keterlambatan = hitungKeterlambatan(hutang.tanggalJatuhTempo, tanggalSekarang)
                    if (keterlambatan > 0) {
                        val denda = hutang.totalDenda
                        val totalHutang = hutang.nominalpinjaman + denda
                        Log.d("HutangCalculator", "Hutang PERHITUNGAN terlambat: keterlambatan=$keterlambatan, denda=$denda, nominal=${hutang.nominalpinjaman}, totalHutang=$totalHutang")
                        totalHutang
                    } else {
                        Log.d("HutangCalculator", "Hutang PERHITUNGAN belum jatuh tempo: keterlambatan=$keterlambatan, nominal=${hutang.nominalpinjaman}")
                        hutang.nominalpinjaman
                    }
                } else {
                    Log.d("HutangCalculator", "Hutang PERHITUNGAN sudah LUNAS, totalHutang=${hutang.nominalpinjaman}")
                    hutang.nominalpinjaman
                }
            }
            HutangType.TEMAN -> {
                Log.d("HutangCalculator", "Hutang TEMAN: totalHutang=${hutang.nominalpinjaman}, statusBayar=${hutang.statusBayar}")
                hutang.nominalpinjaman
            }
            else -> {
                Log.e("HutangCalculator", "HutangType tidak valid: ${hutang.hutangType}")
                hutang.nominalpinjaman
            }
        }
    }
}