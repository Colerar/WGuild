package me.hbj233.wguild.data

import cn.nukkit.Player
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.getTodayDate
import me.hbj233.wguild.utils.sendMsgAndScreenTitle
import me.hbj233.wguild.utils.sendMsgWithTitle
import top.wetabq.easyapi.api.defaults.EconomyAPI
import top.wetabq.easyapi.module.defaults.ScreenShowModule
import top.wetabq.easyapi.screen.ScreenShow
import top.wetabq.easyapi.screen.ShowType
import top.wetabq.easyapi.utils.color
import kotlin.math.max

data class WGuildData(
        var guildDisplayName: String,
        var guildDescription: String,
        var isVisible: Boolean,
        var usingPositionSettings: String,
        var usingSettings: String,
        var canPVP: Boolean,

        var guildLevel: Int,
        var guildMaxMember: Int,
        var guildNowMember: Int,
        var guildMoney: Int,
        var guildActivity: Double,
        val createDate: ArrayList<Int> = arrayListOf(3),
        val guildPlayersData: LinkedHashMap<String, WGuildPlayerData>,
        // K: WHO WAS INVITED V: WHO INVITED
        val guildInvitedPlayers: LinkedHashMap<String, String>,
        val guildAskJoinPlayersName: ArrayList<String>
) {

    private fun getCreateDateString(): String = "${createDate[0]}年${createDate[1]}月${createDate[2]}"

    fun getGuildInformation() = "&7公会名:&e${this.guildDisplayName}\n".color() +
            "&r&7公会介绍:&e${this.guildDescription}\n".color() +
            "&r&7创建日期&e${this.getCreateDateString()}\n".color() +
            "&r&7公会等级: &6&lLv.&e${this.guildLevel}\n".color() +
            "&r&7公会成员数:&e${this.guildNowMember}/${this.guildMaxMember}\n".color() +
            "&r&7公会钱包:&e${this.guildMoney}\n".color() +
            "&r&7公会活跃度:&e${this.guildActivity}\n".color() +
            "&r&7公会成员名单:\n".color() + getMembersList()

    private fun getMembersList(): String {
        var playerList = ""
        guildPlayersData.keys.forEach {
            playerList += "&e$it\n"
        }
        if (playerList.isBlank()) {
            playerList = "&c&l无"
        }
        return playerList.color()
    }

    fun getOnlinePlayersName(): Collection<String> =
            guildPlayersData.keys.filter { WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(it) is Player }

    fun getOnlinePlayers(): Collection<Player> =
            guildPlayersData.keys
                    .filter { WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(it) is Player }
                    .fold(arrayListOf()) { i, it -> i.add(WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(it)); i }

    fun getOfflinePlayersName(): Collection<String> =
            guildPlayersData.keys.filterNot {
                getOnlinePlayersName().contains(it)
            }.toMutableList()

    private fun getSetting(): WGuildSettingData =
            WGuildModule.wguildSettingsConfig.safeGetData(usingSettings)

    private fun getLevelsGroup(): LinkedHashMap<String, WGuildLevelData> = getSetting().guildLevelsSetting.levelsGroup

    fun getLevelAttribute(): WGuildLevelData =
            getLevelsGroup()[guildLevel.toString()] ?: getLevelDataDefault()

    fun getPositionsGroup(): LinkedHashMap<String, WGuildPositionData> =
            WGuildModule.wguildPositionsGroupsConfig.safeGetData(usingPositionSettings).positionsGroup

    fun getPlayerPosition(playerName: String): WGuildPositionData = getPositionsGroup()[guildPlayersData[playerName]?.position]
            ?: WGuildModule.defaultPositionPair.third

    fun guildBroadcastMessage(message: String) {
        getOnlinePlayers().forEach {
            it.sendMsgWithTitle("&e[&b&l$guildDisplayName&r&e] ≫ $message")
        }
    }

    fun guildBroadcastMessageAndTitle(message: String, title: String, subTitle: String = "") {
        getOnlinePlayers().forEach {
            it.sendMsgAndScreenTitle("&e[&b&l$guildDisplayName&r&e] ≫ $message", title, subTitle)
        }
    }

    fun guildBroadcastTip(message: String) {
        ScreenShowModule.addScreenShow(ScreenShow(getOnlinePlayers(), "&e[&b&l$guildDisplayName&r&e] $message", ScreenShowModule.MID_PRIORITY, 1000 * 5, 1000 * 5, true, false, ShowType.TIP))
    }

    fun editGuildInfo(player: Player, guildDisplayName: String = this.guildDisplayName,
                      guildDescription: String = this.guildDescription,
                      isVisible: Boolean = this.isVisible,
                      usingPositionSettings: String = this.usingPositionSettings,
                      usingSettings: String = this.usingSettings,
                      canPVP: Boolean = this.canPVP): Boolean {
        if (!player.hasEditPermission(guildDisplayName.contains(Regex("([&§])")), getLevelAttribute().canUseColor, "彩色公会命名") { this.guildDisplayName = guildDisplayName }) return false
        if (!player.hasEditPermission(isVisible, getLevelAttribute().canVisible, "全服公开") { this.isVisible = isVisible }) return false
        if (!player.hasEditPermission(usingPositionSettings != this.usingPositionSettings, getLevelAttribute().canChangePositionSetting, "修改职位配置") { this.usingPositionSettings = usingPositionSettings }) return false
        if (!player.hasEditPermission(usingSettings != this.usingSettings, player.isOp, "修改配置组") { this.usingSettings = usingSettings }) return false
        if (!player.hasEditPermission(canPVP, getLevelAttribute().canPVP, "开启PVP") { this.canPVP = canPVP }) return false
        this.guildDescription = guildDescription
        WGuildModule.wguildConfig.save()
        this.guildBroadcastMessageAndTitle("&a公会信息已经修改", "&a公会信息已经修改", "&7快去看看有那些更新吧！")
        return true
    }

    fun Player.hasEditPermission(hasUsed: Boolean, hasPermission: Boolean, permissionName: String, editAction: () -> Unit): Boolean {
        if (hasUsed) {
            if (hasPermission || isOp) {
                editAction()
            } else {
                this.sendMsgWithTitle("&c公会等级或权限不足, 没有修改 $permissionName 的权限")
                return false
            }
        } else {
            editAction()
        }
        return true
    }

    fun disbandGuild(player: Player, guildId: String) {
        if (player.isOp || getPlayerPosition(player.name).canDisband) {
            guildBroadcastMessageAndTitle("&c&l${player.name} 解散了公会", "&c&l${player.name} 解散了公会", "&7默哀")
            guildBroadcastMessage("&c&l公会解散后，公会资金将不会返还！")
            guildBroadcastTip("&c&l公会被 ${player.name} 解散！")
            guildInvitedPlayers.forEach {
                WGuildModule.wguildPlayerConfig.safeGetData(it.key).receivedInvite.remove(guildId)
                WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(it.key)?.sendMsgWithTitle("&c&l你被邀请的公会 &b${guildDisplayName} 已经被解散，自动取消邀请")
            }
            guildAskJoinPlayersName.forEach {
                WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(it)?.sendMsgWithTitle("&c&l你申请的公会 &b${guildDisplayName} 已经被解散，自动取消申请")
            }
            guildPlayersData.forEach {
                kickPlayer(it.key, false)
            }
            guildDisplayName = "已被删除"
            guildDescription = "已被删除"
            isVisible = false
            guildPlayersData.clear()
            guildInvitedPlayers.clear()
            guildAskJoinPlayersName.clear()
            WGuildModule.wguildConfig.simpleConfig.remove(guildId)
            WGuildModule.wguildConfig.save()
        } else {
            player.sendMsgWithTitle("&c&l你没有权限解散公会")
        }
    }

    fun applyJoin(playerName: String) {
        if (!guildAskJoinPlayersName.contains(playerName)) {
            guildAskJoinPlayersName.add(playerName)
            guildBroadcastMessageAndTitle("&d玩家 &c&l$playerName &r&d申请加入公会", "&c&l$playerName &r&d申请加入公会", "&7快去处理一下申请请求吧!")
            WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&e申请成功!")
            WGuildModule.wguildConfig.save()
        } else {
            WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&c您已经申请过了")
        }
    }

    fun joinPlayer(playerName: String, guildId: String, position: String = getPositionsGroup().filter { it.value.isDefault }.keys.lastOrNull()
            ?: getPositionsGroup().keys.first(), notice: Boolean) {
        if (!guildPlayersData.containsKey(playerName)) {
            if (guildNowMember + 1 <= guildMaxMember) {
                val playerData = WGuildModule.wguildPlayerConfig.safeGetData(playerName)
                playerData.playerJoinGuildId = guildId
                guildPlayersData[playerName] = WGuildPlayerData(position, getTodayDate(), 0.0, 0)
                guildNowMember++
                if (notice) {
                    guildBroadcastMessageAndTitle("&6玩家 &c&l$playerName &r&6加入了公会， &d在公会中担任 &a&l$position &r&d的职位", "&c&l$playerName &r&6加入了公会", "&7快来欢迎新成员吧!")
                    WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&a&l成功加入公会 &c$guildDisplayName")
                }
                WGuildModule.wguildConfig.save()
            } else {
                this.guildBroadcastMessage("&6玩家 &c&l$playerName &r&6尝试加入公会，&c&l但公会已满，无法加入")
                WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&c&l公会人已经满了, 无法进入")
            }
        } else {
            if (notice) WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&c&l你已经在这个公会里面了")
        }
    }

    fun kickPlayer(playerName: String, notice: Boolean = true) {
        if (guildPlayersData.containsKey(playerName)) {
            val playerData = WGuildModule.wguildPlayerConfig.safeGetData(playerName)
            playerData.playerJoinGuildId = ""
            reduceActivity(playerName, "玩家退出了公会", guildPlayersData[playerName]?.contributedActivity ?: 0.0)
            guildPlayersData.remove(playerName)
            if (notice) {
                guildBroadcastMessageAndTitle("&7&l$playerName &r&c退出了公会", "&7&l$playerName &r&c退出了公会", "&7欢送他离开吧!")
                WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&c&l你退出了公会 &b$guildDisplayName")
            }
            WGuildModule.wguildConfig.save()
        } else {
            if (notice) WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgWithTitle("&c&l你已经退出这个公会了")
        }
    }

    fun upgradeGuild(player: Player) {
        val maxLevel = getLevelsGroup().keys.fold(0) { acc, s -> max(s.toInt(), acc) }
        if (guildLevel < maxLevel) {
            getLevelsGroup()[(guildLevel + 1).toString()]?.let { nextLevelData ->
                if (EconomyAPI.compatibilityCheck.isCompatible() && EconomyAPI.getMoney(player) ?: 0.0 >= nextLevelData.price || !EconomyAPI.compatibilityCheck.isCompatible()) {
                    EconomyAPI.reduceMoney(player, nextLevelData.price.toDouble())
                    guildLevel++
                    this.guildBroadcastMessageAndTitle("&c&l${player.name} &r&a将公会从 &l&6Lv.&e${guildLevel - 1} &r&a升级到 &l&6Lv.&e${guildLevel}",
                            "&a公会从 &l&6Lv.&e${guildLevel - 1} &r&a升级到 &l&6Lv.&e${guildLevel}",
                            "&7快来庆祝吧！")
                }
            }
        } else {
            player.sendMsgWithTitle("&c&l你的公会已经满级了，无法继续升级")
        }
    }

    fun addMoney(playerName: String, reason: String, count: Int, reducePlayerMoney: Boolean = true) {
        if (count > 0) {
            if (reducePlayerMoney) if (EconomyAPI.compatibilityCheck.isCompatible()) EconomyAPI.reduceMoney(playerName, count.toDouble())
            guildMoney += count
            WGuildModule.wguildConfig.save()
            guildBroadcastMessage("&c&l$playerName &r&6为公会增加了 &a&l$count &r&6资金, &d原因是: $reason")
        }
    }

    fun reduceMoney(playerName: String, reason: String, count: Int) {
        if (count > 0) {
            guildMoney -= count
            WGuildModule.wguildConfig.save()
            guildBroadcastMessage("&c&l$playerName &r&6使用了公会 &a&l$count &r&6资金, &d原因是: $reason")
        }
    }

    fun addActivity(causeBy: String, reason: String, count: Double) {
        if (count > 0) {
            guildActivity += count
            WGuildModule.wguildConfig.save()
            guildBroadcastMessage("&c&l$causeBy &r&6使得公会添加了 &a&l$count &r&6活跃度, &d原因是: $reason")
        }
    }

    fun reduceActivity(causeBy: String, reason: String, count: Double) {
        if (count > 0) {
            guildActivity -= count
            WGuildModule.wguildConfig.save()
            guildBroadcastMessage("&c&l$causeBy &r&6使得公会减少了 &a&l$count &r&6活跃度, &d原因是: $reason")
        }
    }

}