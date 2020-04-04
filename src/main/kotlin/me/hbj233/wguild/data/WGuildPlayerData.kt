package me.hbj233.wguild.data

data class WGuildPlayerData(
        var position: String,
        val joinDate: ArrayList<Int> = arrayListOf(3),
        val contributedActivity: Double,
        val donatedMoney: Int
) {
    fun getJoinDateString(): String = "${joinDate[0]}年${joinDate[1]}月${joinDate[2]}"

}