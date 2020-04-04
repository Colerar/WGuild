package me.hbj233.wguild.utils

fun getRandomString(): String {
    return ('A'..'z').map { it }.shuffled().subList(0, 3).joinToString("")
}
