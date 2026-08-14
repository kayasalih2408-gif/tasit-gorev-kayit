package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    val turkishMonths = listOf(
        "Tüm Aylar",
        "Ocak",
        "Şubat",
        "Mart",
        "Nisan",
        "Mayıs",
        "Haziran",
        "Temmuz",
        "Ağustos",
        "Eylül",
        "Ekim",
        "Kasım",
        "Aralık"
    )

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
        return sdf.format(Date())
    }

    fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
        return sdf.format(Date())
    }

    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH) + 1 // 1-12
    }

    fun getMonthName(month: Int): String {
        return if (month in 1..12) turkishMonths[month] else "Geçersiz Ay"
    }

    fun parseDateToTimestamp(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun generateDutyNumber(year: Int, month: Int, sequence: Int): String {
        return String.format(Locale("tr", "TR"), "%d/%02d-%03d", year, month, sequence)
    }
}
