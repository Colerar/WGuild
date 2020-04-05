package me.hbj233.wguild.data

data class WGuildPlayerData(
        var position: String,
        val joinDate: ArrayList<Int> = arrayListOf(3),
        var contributedActivity: Double,
        var donatedMoney: Int,
        var signInData: Long = -1L
) {
    fun getJoinDateString(): String = "${joinDate[0]}年${joinDate[1]}月${joinDate[2]}"

}