package me.hbj233.wguild.utils

import java.util.*

fun getTodayDate(): ArrayList<Int> {
    val calendar = Calendar.getInstance()
    return arrayListOf(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
}