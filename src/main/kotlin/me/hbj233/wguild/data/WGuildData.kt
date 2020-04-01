package me.hbj233.wguild.data

import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.module.WGuildModule
import top.wetabq.easyapi.utils.color
import kotlin.collections.LinkedHashMap

data class WGuildData(
        var guildDisplayName: String,
        val guildLevel: Int,
        var guildDescription: String,
        var guildMaxMember: Int,
        val guildNowMember: Int,
        val guildMoney: Int,
        val createDate: ArrayList<Int> = arrayListOf(3),
        val guildActivity: Double,
        var isVisible : Boolean,
        val canChangeUsingSettings : Boolean,
        val guildPlayersData: LinkedHashMap<String, WGuildPlayerData>,
        // K: WHO INVITED V: WHO WAS INVITED
        val guildInvitedPlayers: LinkedHashMap<String,String>,
        val guildAskJoinPlayersName : ArrayList<String>,
        var usingSettings: String
) {

    private fun getCreateDateString() : String =  "${createDate[0]}年${createDate[1]}月${createDate[2]}"

    fun getGuildInformation() = "&7公会名:&e${this.guildDisplayName}\n".color() +
            "&r&7公会介绍:&e${this.guildDescription}\n".color() +
            "&r&7创建日期&e${this.getCreateDateString()}\n".color() +
            "&r&7公会等级:&e${this.guildLevel}\n".color() +
            "&r&7公会成员数:&e${this.guildNowMember}/${this.guildMaxMember}\n".color() +
            "&r&7公会钱包:&e${this.guildMoney}\n".color()+
            "&r&7公会活跃度:&e${this.guildActivity}\n".color() +
            "&r&7公会成员名单:\n".color() + getMembersList()

    private fun getMembersList() : String{
        var playerList : String = ""
        guildPlayersData.keys.forEach {
            playerList += "&e$it\n"
        }
        if (playerList.isBlank()){
            playerList = "&c&l无"
        }
        return playerList.color()
    }

    fun getOnlinePlayersName() : MutableCollection<String>{
        val onlineWGuildPlayersName: MutableCollection<String> = arrayListOf()
        WGuildPlugin.instance.server.onlinePlayers.values.forEach {
            if (guildPlayersData.containsKey(it.name)) {
                onlineWGuildPlayersName.add(it.name)
            }
        }
        return onlineWGuildPlayersName
    }

    fun getOfflinePlayersName() : MutableCollection<String> = guildPlayersData.keys.filterNot {
        getOnlinePlayersName().contains(it)
    }.toMutableList()

    fun getSetting(): WGuildSettingData = WGuildModule.wguildSettingsConfig.safeGetData(usingSettings)

    fun getLevelsGroup() : LinkedHashMap<String, String> =
            WGuildModule.wguildLevelsGroupConfig.safeGetData(getSetting().guildLevelsSetting).levelsGroup

    fun getLevels() : LinkedHashMap<Int, WGuildLevelData>{
        val levels = linkedMapOf<Int, WGuildLevelData>()
        getLevelsGroup().values.forEach {
            levels[it.toInt()] = WGuildModule.wguildLevelsConfig.safeGetData(it)
        }
        return levels
    }

    fun getPositionsGroup() : ArrayList<String> =
            WGuildModule.wguildPositionsGroupsConfig.safeGetData(getSetting().guildPositions).positionsGroup

    fun getPositions() : MutableList<WGuildPositionData> {
        val positions = mutableListOf<WGuildPositionData>()
        getPositionsGroup().forEach {
            positions.add(WGuildModule.wguildPositionsConfig.safeGetData(it))
        }
        return positions
    }

}