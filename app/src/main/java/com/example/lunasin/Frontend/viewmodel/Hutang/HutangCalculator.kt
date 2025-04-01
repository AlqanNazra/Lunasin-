package com.example.lunasin.Frontend.viewmodel.Hutang

import android.util.Log
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object HutangCalculator {
    // Hitung denda
    fun dendaBunga_Tahunan(sisahutang:Double, bunga:Double, telat:Int): Double
    {
        val  Per_bunga = bunga / 100
        val denda1 = Per_bunga/365
        val denda2 = denda1 * sisahutang
        return denda2 * telat
    }

    fun dendaBunga_Bulan(sisahutang:Double, bunga:Double, telat:Int): Double
    {
        val  Per_bunga = bunga / 100
        val denda1 = Per_bunga/12
        val denda2 = denda1 * sisahutang
        return denda2 * telat
    }

    fun dendaTetap (denda : Double, Telat : Double) : Double
    {
        return denda * Telat
    }

    fun denda_Cicilan(sisahutang:Double, bunga:Double, telat:Int): Double
    {
        val  Per_bunga = bunga / 100
        val denda1 = Per_bunga * sisahutang
        return denda1 * telat
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

    fun debt_Service_Coverage_Ratio(pendapatan: Double,totalcicilan: Double): String {
        val dscr = totalcicilan / pendapatan
        return if (dscr >= 1)
        {
            "Aman : Resiko Gagal bayar aman"
        }
        else
        {
            "Warning: Resiko Gagal bayar tinggi."
        }
    }

    fun rumus_Amorsiasi(angsuranPerbulan: Double, bunga: Double, lamaPinjam: Int): Double {
        val bunga1 = bunga/100
        val total1 = 1 + bunga1
        val total2_pembagi = pangkat(total1,lamaPinjam) - 1

        val total1_penyebut = pangkat(total1,lamaPinjam)
        val total2_penyebut = total1_penyebut * bunga1 * angsuranPerbulan

        return total2_penyebut/total2_pembagi
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