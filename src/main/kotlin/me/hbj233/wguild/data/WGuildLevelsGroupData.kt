package me.hbj233.wguild.data

data class WGuildLevelsGroupData(
        // MutableMap K: level of level settings, K:  WGuildLevelData's identity.
        var levelsGroup: LinkedHashMap<String, WGuildLevelData>
)