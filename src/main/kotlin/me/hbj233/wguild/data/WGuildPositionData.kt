package me.hbj233.wguild.data

import top.wetabq.easyapi.utils.color

data class WGuildPositionData(
        val displayName : String,
        val isOwner : Boolean,
        val canKick : Boolean,
        val canInvite : Boolean,
        val canPermitInvite : Boolean,
        val canChangeSetting : Boolean,
        val canDissolve : Boolean,
        val canManageMoney : Boolean
) {
    init {
        displayName.color()
    }
}