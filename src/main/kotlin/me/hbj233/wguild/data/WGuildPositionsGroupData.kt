package me.hbj233.wguild.data

data class WGuildPositionsGroupData(
        //LIST OF POSITION KEY.
        var isDefaultSetting: Boolean = false,
        var ownerGuild: String,
        val positionsGroup: LinkedHashMap<String, WGuildPositionData>

)