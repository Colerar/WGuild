package me.hbj233.wguild.data

import top.wetabq.easyapi.utils.color

fun getPositionDataDefault(): WGuildPositionData = WGuildPositionData("&7&l示例权限", isOwner = false, canKick = false,
        canInvite = true, canPermitInvite = false, canChangeSetting = false,
        canDisband = false, canManageMoney = false, isDefault = false)

data class WGuildPositionData(
        val displayName: String,
        val isOwner: Boolean,
        val canKick: Boolean,
        val canInvite: Boolean,
        val canPermitInvite: Boolean,
        val canChangeSetting: Boolean,
        val canDisband: Boolean,
        val canManageMoney: Boolean,
        val isDefault: Boolean
) {
    init {
        displayName.color()
    }
}