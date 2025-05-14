package com.example.lunasin.utils


import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

    fun normalizeDate(dateStr: String): String {
        return try {
            val possibleFormats = listOf("d/M/yyyy", "dd/MM/yyyy")
            val date = possibleFormats.firstNotNullOfOrNull { format ->
                try {
                    LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format))
                } catch (e: Exception) {
                    null
                }
            } ?: return dateStr
            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            Log.e("TanggalError", "Gagal normalisasi tanggal: $dateStr", e)
            dateStr
        }
    }