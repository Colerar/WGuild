package me.hbj233.wguild.data

data class PlayerGuildData(

        var playerJoinGuildId: String,
        var isGuildChatMode: Boolean,
        val receivedInvite: LinkedHashMap<String, String> // GuildId -> Player

)