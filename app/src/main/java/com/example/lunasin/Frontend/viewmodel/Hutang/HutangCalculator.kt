package com.example.lunasin.Frontend.viewmodel.Hutang

import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow

object HutangCalculator {

    // DIUBAH: Nama fungsi diubah ke camelCase
    fun hitungDendaBungaTahunan(sisaHutang: Double, bungaTahunan: Double, hariTelat: Int): Double {
        val bungaPerSeratus = bungaTahunan / 100
        val dendaPerHari = bungaPerSeratus / 365
        val dendaDariPokok = dendaPerHari * sisaHutang
        return dendaDariPokok * hariTelat
    }

    // DIUBAH: Nama fungsi diubah ke camelCase
    fun hitungDendaBungaBulanan(sisaHutang: Double, bungaTahunan: Double, bulanTelat: Int): Double {
        val bungaPerSeratus = bungaTahunan / 100
        val dendaPerBulan = bungaPerSeratus / 12
        val dendaDariPokok = dendaPerBulan * sisaHutang
        return dendaDariPokok * bulanTelat
    }

    // DIUBAH: Nama fungsi dan parameter diubah ke camelCase
    fun hitungDendaTetap(denda: Double, telat: Double): Double {
        return denda + telat
    }

    // DIUBAH: Menggunakan .pow() untuk efisiensi dan kejelasan
    fun hitungCicilanPerbulan(nominalPinjaman: Double, bungaTahunan: Double, lamaPinjamBulan: Int): Double {
        if (lamaPinjamBulan == 0) return 0.0

        val bungaBulanan = (bungaTahunan / 100) / 12
        if (bungaBulanan == 0.0) return nominalPinjaman / lamaPinjamBulan

        val pembilang = nominalPinjaman * bungaBulanan * (1 + bungaBulanan).pow(lamaPinjamBulan)
        val penyebut = (1 + bungaBulanan).pow(lamaPinjamBulan) - 1

        return if (penyebut != 0.0) pembilang / penyebut else 0.0
    }

    // DIUBAH: Nama fungsi diubah ke camelCase
    fun hitungTotalBunga(nominalPinjaman: Double, bungaTahunan: Double, lamaPinjamBulan: Int): Double {
        val cicilan = hitungCicilanPerbulan(nominalPinjaman, bungaTahunan, lamaPinjamBulan)
        val totalPembayaran = cicilan * lamaPinjamBulan
        return totalPembayaran - nominalPinjaman
    }

    // --- FUNGSI PALING PENTING YANG DIPERBAIKI ---
    // Menerima Calendar, mengembalikan Date untuk mengatasi crash
    fun hitungTanggalAkhir(startCalendar: Calendar, lamaPinjamBulan: Int): Date {
        // clone() penting agar tidak mengubah tanggal asli di ViewModel
        val calendar = startCalendar.clone() as Calendar
        calendar.add(Calendar.MONTH, lamaPinjamBulan)
        return calendar.time // Kembalikan sebagai objek Date
    }

    // DIUBAH: Nama fungsi diubah ke camelCase
    fun hitungTotalHutang(nominalPinjaman: Double, bungaTahunan: Double, lamaPinjamBulan: Int): Double {
        val totalBunga = hitungTotalBunga(nominalPinjaman, bungaTahunan, lamaPinjamBulan)
        return nominalPinjaman + totalBunga
    }

    // DIUBAH: Fungsi ini sekarang mengembalikan Double, bukan String.
    // Logika untuk menampilkan "Warning/Aman" sebaiknya ada di UI/ViewModel.
    fun hitungDebtToIncomeRatio(totalPembayaranBulanan: Double, pendapatanBulanan: Double): Double {
        if (pendapatanBulanan == 0.0) return Double.POSITIVE_INFINITY
        return (totalPembayaranBulanan / pendapatanBulanan) * 100
    }

    fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }
}