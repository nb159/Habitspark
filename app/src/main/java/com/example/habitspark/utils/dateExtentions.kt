package com.example.habitspark.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
fun Date.toLocalDate(): LocalDate {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

//convert minutes to readable hh:mm format
fun minutesToHoursMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return String.format("%02d:%02d", hours, remainingMinutes)
}

fun minutesToDecimalHours(minutes: Int): String {
    val hours = minutes / 60.0
    return if (hours % 1 == 0.0) {
        // It's a whole number, like 10.0 → "10"
        hours.toInt().toString()
    } else {
        // Round to 2 decimal places, like 10.5 → "10.50"
        String.format("%.2f", hours)
    }
}
