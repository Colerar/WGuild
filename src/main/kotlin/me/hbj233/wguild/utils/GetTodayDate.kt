package me.hbj233.wguild.utils

import java.text.SimpleDateFormat
import java.util.*


fun getTodayDate(): ArrayList<Int> {
    val calendar = Calendar.getInstance()
    return arrayListOf(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
}

fun Long.toNormalData(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(Date(java.lang.String.valueOf(this).toLong()))
}