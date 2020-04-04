package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementInput
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.sendMsgAndScreenTitle
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.FormSimple
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.utils.color

fun getWGuildData(player: Player): Map.Entry<String, WGuildData>? {
    WGuildModule.wguildConfig.simpleConfig.forEach {
        if (it.value.guildPlayersData.keys.contains(player.name)) {
            return it
        }
    }
    return null
}

class MyWGuildGUI(player: Player) : ResponsibleFormWindowSimple(
        "${WGuildPlugin.title}&e我的公会面板".color(), "") {

    init {
        getWGuildData(player)?.let { entry ->
            val targetWGuildData = entry.value
            val targetWGuildId = entry.key
            val button1Text = "我的公会信息 - ${targetWGuildData.guildDisplayName}"
            addButton(button1Text) { player ->
                player.showFormWindow(object : FormSimple(
                        WGuildPlugin.title + button1Text,
                        targetWGuildData.getGuildInformation()
                ) {
                    override fun onClosed(player: Player) {
                        player.showFormWindow(MyWGuildGUI(player))
                    }
                })
            }

            addButton("查看公会玩家") { player ->
                player.showFormWindow(MyWGuildPlayerList(targetWGuildData))
            }

            val targetPlayerPos = targetWGuildData.guildPlayersData[player.name]?.position
            if (targetPlayerPos != null) {
                if (targetWGuildData.getPositionsGroup()[targetPlayerPos]?.canChangeSetting == true) {

                    addButton("更改公会设置") { player ->
                        val newConfigGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a修改公会设置".color()) {
                            init {
                                addElement(ElementInput("公会名称", "请输入", targetWGuildData.guildDisplayName))
                                addElement(ElementInput("公会介绍", "请输入", targetWGuildData.guildDescription))
                                addElement(ElementToggle("公会是否公开", targetWGuildData.isVisible))
                                addElement(ElementToggle("是否允许公会内PVP", targetWGuildData.canPVP))
                            }

                            override fun onClicked(response: FormResponseCustom, player: Player) {
                                targetWGuildData.let {
                                    val guildDisplayName = response.getInputResponse(0)
                                    val guildDescription = response.getInputResponse(1)
                                    val isVisible = response.getToggleResponse(2)
                                    val canPVP = response.getToggleResponse(3)
                                    targetWGuildData.editGuildInfo(
                                            player = player,
                                            guildDisplayName = guildDisplayName,
                                            guildDescription = guildDescription,
                                            isVisible = isVisible,
                                            canPVP = canPVP
                                    )
                                }

                            }

                            override fun onClosed(player: Player) {
                                player.showFormWindow(MyWGuildGUI(player))
                            }
                        }
                        player.showFormWindow(newConfigGUI)
                    }

                }

                addButton("更改玩家职位") { player ->
                    val changePosGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a更改玩家职位".color()) {

                        init {

                            this.addElement(ElementLabel("请您先选择玩家, 之后选择变换的职位"))
                            this.addElement(ElementDropdown("玩家", targetWGuildData.guildPlayersData.keys.toMutableList()
                                    .also {
                                        it.add(0, "请选择")
                                        it.remove(player.name)
                                    }, 0))
                            this.addElement(ElementDropdown("职位", targetWGuildData.getPositionsGroup().keys.toMutableList().also {
                                it.add(0, "原职位")
                            }, 0))

                        }

                        override fun onClicked(response: FormResponseCustom, player: Player) {
                            val playerName = response.getDropdownResponse(1).elementContent
                            val lastPos = targetWGuildData.guildPlayersData[playerName]?.position ?: ""
                            val pos = when (val response1 = response.getDropdownResponse(1).elementContent) {
                                "原职位" -> return
                                else -> response1
                            }

                            if (playerName != "请选择") {
                                if (targetWGuildData.guildPlayersData.containsKey(playerName)) {
                                    targetWGuildData.guildPlayersData[playerName]?.position = pos ?: lastPos
                                    targetWGuildData.guildBroadcastMessage("&a玩家 &c&l$playerName &r&a的职位, 从 $lastPos 变动到 $pos")
                                    WGuildModule.wguildConfig.save()
                                }
                            }
                        }

                        override fun onClosed(player: Player) {
                            player.showFormWindow(MyWGuildGUI(player))
                        }

                    }

                    player.showFormWindow(changePosGUI)

                }
                addButton("邀请玩家进入") { player ->

                    val invitePlayerGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a邀请玩家进入".color()) {

                        init {

                            val playerNameList: MutableList<String> = mutableListOf()
                            WGuildPlugin.instance.server.onlinePlayers.values.forEach {
                                playerNameList.add(it.name)
                            }

                            addElement(ElementLabel("&e您可以选择一名在线玩家以邀请其进入公会.".color()))
                            addElement(ElementDropdown("要邀请的玩家", playerNameList.also {
                                it.add(0, "请选择")
                            }))

                        }

                        override fun onClicked(response: FormResponseCustom, player: Player) {

                            when (response.getDropdownResponse(1).elementContent) {
                                "请选择" -> player.sendMsgWithTitle("&c&l操作失败, 您未选择要邀请的玩家.")
                                else -> {
                                    val playerName = response.getDropdownResponse(1).elementContent
                                    targetWGuildData.guildInvitedPlayers[playerName] = player.name
                                    player.sendMsgWithTitle("&e操作成功! 您向名为 $playerName 的玩家发送了邀请.".color())
                                    WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)
                                            .takeIf { it is Player }
                                            ?.sendMsgAndScreenTitle(
                                                    "&e&l你收到了一封来自 &c&l${player.name} &e&l的邀请, 邀请您加入公会 &b&l${targetWGuildData.guildDisplayName}",
                                                    "&c&l> &r&e你被邀请加入 &b&l${targetWGuildData.guildDisplayName} &r&c&l<",
                                                    "&7输入 &l/wgb g &r&7打开公会主面板以查看邀请请求")
                                }
                            }

                        }

                        override fun onClosed(player: Player) {
                            player.showFormWindow(MyWGuildGUI(player))
                        }

                    }

                    player.showFormWindow(invitePlayerGUI)

                }

                addButton("查看申请列表") { player ->
                    val viewApplicationGUI = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&a申请列表") {

                        init {
                            targetWGuildData.guildAskJoinPlayersName.forEach { applicationPlayerName ->
                                addButton(applicationPlayerName) { p ->
                                    val confirmGUI = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&a同意申请?", "&a玩家 &c&l${applicationPlayerName} &r&e请求加入公会") {

                                        init {
                                            val playerData = WGuildModule.wguildPlayerConfig.safeGetData(applicationPlayerName)
                                            addButton("&a&l同意") { p ->
                                                if (playerData.playerJoinGuildId == "") {
                                                    targetWGuildData.guildInvitedPlayers.remove(p.name);
                                                    targetWGuildData.joinPlayer(p.name, targetWGuildId)
                                                } else {
                                                    p.sendMsgWithTitle("&c该玩家已经存在与另一个公会之中")
                                                }
                                            }
                                            addButton("&c&l拒绝") { p ->
                                                targetWGuildData.guildAskJoinPlayersName.remove(p.name)
                                            }
                                            addButton("&7让我再想想...")
                                        }

                                    }

                                    p.showFormWindow(confirmGUI)

                                }
                            }
                        }

                        override fun onClosed(player: Player) {
                            player.showFormWindow(MyWGuildGUI(player))
                        }

                    }

                    player.showFormWindow(viewApplicationGUI)
                }
                addButton("踢出玩家") { player ->

                }
            }

            addButton("退出公会") { player ->

            }

        }

    }

    override fun onClosed(player: Player) {
        player.showFormWindow(WGuildMainGUI(player))
    }

}

class MyWGuildPlayerList(private val targetWGuildData: WGuildData) : ResponsibleFormWindowSimple("在线公会玩家- ${targetWGuildData.guildDisplayName}".color(), "") {

    init {

        targetWGuildData.getOnlinePlayersName().forEach {
            addButton("&a${it} - 在线".color()) { player -> player.showFormWindow(WGuildMembersGUI(it, targetWGuildData, true)) }
        }

        targetWGuildData.getOfflinePlayersName().forEach {
            addButton("&e${it} - 离线".color()) { player ->
                player.showFormWindow(WGuildMembersGUI(it, targetWGuildData, false))
            }
        }

    }
}


class WGuildMembersGUI(playerName: String, targetWGuildData: WGuildData, isOnline: Boolean) : ResponsibleFormWindowSimple("${WGuildPlugin.title}公会成员 $playerName 的信息") {

    init {

        val targetPlayerData = targetWGuildData.guildPlayersData[playerName]

        targetPlayerData?.let {
            content = if (isOnline) {
                "&r&7在线状态:&e在线\n".color()
            } else {
                "&r&7在线状态:&c离线\n".color()
            }
            content += "&r&7职位:&e${it.position}\n".color() +
                    "&r&7加入日期:&e${it.getJoinDateString()}\n".color() +
                    "&r&7贡献活跃度:&e${it.contributedActivity}\n".color() +
                    "&r&7捐献资金:&e${it.donatedMoney}\n".color()
        }

    }

    override fun onClosed(player: Player) {
        player.showFormWindow(getWGuildData(player)?.let { MyWGuildPlayerList(it.value) })
    }

}