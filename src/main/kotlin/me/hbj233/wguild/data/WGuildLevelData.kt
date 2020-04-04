package me.hbj233.wguild.data

fun getLevelDataDefault(): WGuildLevelData = WGuildLevelData(5, canChangePositionSetting = false, canChangeName = false, canUseColor = false, canVisible = false, canPVP = true, price = 250)

data class WGuildLevelData(
        val maxMembers: Int,
        val canChangePositionSetting: Boolean,
        val canChangeName: Boolean,
        val canUseColor: Boolean,
        val canVisible: Boolean,
        val canPVP: Boolean,
        val price: Int
)