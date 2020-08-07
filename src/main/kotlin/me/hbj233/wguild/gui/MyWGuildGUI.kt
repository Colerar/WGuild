package me.hbj233.wguild.gui

import  cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementInput
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.popItem
import me.hbj233.wguild.utils.sendMsgAndScreenTitle
import me.hbj233.wguild.utils.sendMsgWithTitle
import me.hbj233.wguild.utils.toNormalData
import moe.him188.gui.window.FormSimple
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.api.defaults.EconomyAPI
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

            addButton("每日签到") { player ->

                targetWGuildData.guildPlayersData[player.name]?.let {
                    if (it.signInData + 1000 * 60 * 60 * 24 <= System.currentTimeMillis()) {
                        popItem(player, targetWGuildData.getOnlinePlayers())
                        it.signInData = System.currentTimeMillis()
                        targetWGuildData.guildBroadcastMessage("&a&l${player.name} &e签到了！")
                        targetWGuildData.addActivity(player.name, "签到", 1.0)
                        if (targetWGuildData.guildMoney >= targetWGuildData.signInAward) {
                            if (targetWGuildData.signInAward != 0) {
                                targetWGuildData.reduceMoney(player.name, "分发签到奖励", targetWGuildData.signInAward)
                                EconomyAPI.addMoney(player.name, targetWGuildData.signInAward.toDouble())
                            } else {
                                player.sendMsgWithTitle("&d&l你的公会没有签到奖励。")
                            }
                        } else {
                            player.sendMsgWithTitle("&d&l真是太惨了，&e公会穷的连你的签到奖励都给不起。")
                        }
                    } else {
                        player.sendMsgWithTitle("&c&l不要重复签到！")
                    }
                }

            }

            addButton("贡献资金") { player ->

                val addMoneyGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a贡献公会资金".color()) {

                    init {
                        addElement(ElementLabel(("&e你可以输入&c&l正整数&r&e的资金数目来对公会进行捐赠，&d捐赠的资金全部只能用于公会运作和升级\n" +
                                "&a你目前拥有的资金为: &e&l${if (EconomyAPI.compatibilityCheck.isCompatible()) EconomyAPI.getMoney(player)?: 0 else "不兼容EconomyAPI"}").color()))
                        addElement(ElementInput("&6&l输入你意向捐赠的资金数目".color(), "请输入"))

                    }

                    override fun onClicked(response: FormResponseCustom, player: Player) {

                        if (response.getInputResponse(1).matches(Regex("\\d{1,10}"))) {
                            val amount = response.getInputResponse(1).toInt()
                            if (amount > 0) {
                                if (EconomyAPI.compatibilityCheck.isCompatible()) {
                                    if (EconomyAPI.getMoney(player)?:0.0 >= amount) {
                                        targetWGuildData.addMoney(player.name, "捐赠", amount)
                                        player.sendMsgWithTitle("&a&l捐赠成功")
                                    } else {
                                        player.sendMsgWithTitle("&c&l你没有足够的资金进行捐赠")
                                    }
                                } else {
                                    player.sendMsgWithTitle("&c&l服务器不兼容EconomyAPI，无法捐赠")
                                }
                            } else {
                                player.sendMsgWithTitle("&c&l贡献的资金必须大于0")
                            }
                        } else {
                            player.sendMsgWithTitle("&c&l贡献的资金必须为正整数")
                        }

                    }

                    override fun onClosed(player: Player) {
                        player.showFormWindow(MyWGuildGUI(player))
                    }

                }
                player.showFormWindow(addMoneyGUI)

            }

            addButton("查看公会玩家") { player ->
                player.showFormWindow(MyWGuildPlayerList(targetWGuildData))
            }

            val targetPlayerPos = targetWGuildData.getPlayerPosition(player.name)
            if (targetPlayerPos.canChangeSetting) {

                addButton("更改公会设置") { player ->
                    val newConfigGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a修改公会设置".color()) {
                        init {
                            addElement(ElementInput("公会名称", "请输入", targetWGuildData.guildDisplayName))
                            addElement(ElementInput("公会介绍", "请输入", targetWGuildData.guildDescription))
                            addElement(ElementToggle("公会是否公开", targetWGuildData.isVisible))
                            addElement(ElementToggle("是否允许公会内PVP", targetWGuildData.canPVP))
                            addElement(ElementInput("公会签到奖励（从公会钱包中扣除）", "请输入", targetWGuildData.signInAward.toString()))
                        }

                        override fun onClicked(response: FormResponseCustom, player: Player) {
                            targetWGuildData.let {
                                val guildDisplayName = response.getInputResponse(0)
                                val guildDescription = response.getInputResponse(1)
                                val isVisible = response.getToggleResponse(2)
                                val canPVP = response.getToggleResponse(3)
                                if (response.getInputResponse(4).matches(Regex("\\d{1,10}"))) {
                                    val signInAward = response.getInputResponse(4).toInt()
                                    if (signInAward >= 0) {
                                        targetWGuildData.editGuildInfo(
                                                player = player,
                                                guildDisplayName = guildDisplayName,
                                                guildDescription = guildDescription,
                                                isVisible = isVisible,
                                                canPVP = canPVP,
                                                signInAward = signInAward
                                        )
                                    } else {
                                        player.sendMsgWithTitle("&c&l设置的签到奖励必须大于等于0")
                                    }
                                } else {
                                    player.sendMsgWithTitle("&c&l设置的签到奖励必须为正整数")
                                }
                            }

                        }

                        override fun onClosed(player: Player) {
                            player.showFormWindow(MyWGuildGUI(player))
                        }
                    }
                    player.showFormWindow(newConfigGUI)
                }

            }

            if (targetPlayerPos.isOwner) {

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
                            val pos = when (val response1 = response.getDropdownResponse(2).elementContent) {
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

            }

            if (targetPlayerPos.canInvite) {
                addButton("邀请玩家进入") { player ->

                    if (targetWGuildData.guildNowMember + 1 <= targetWGuildData.guildMaxMember) {
                        val invitePlayerGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a邀请玩家进入".color()) {

                            init {

                                val playerNameList: MutableList<String> = mutableListOf()
                                WGuildPlugin.instance.server.onlinePlayers.values.forEach {
                                    playerNameList.add(it.name)
                                }

                                addElement(ElementLabel("&e您可以选择一名在线玩家以邀请其进入公会".color()))
                                addElement(ElementDropdown("要邀请的玩家", playerNameList.also {
                                    it.add(0, "请选择")
                                }))

                            }

                            override fun onClicked(response: FormResponseCustom, player: Player) {

                                when (response.getDropdownResponse(1).elementContent) {
                                    "请选择" -> player.sendMsgWithTitle("&c&l操作失败，您未选择要邀请的玩家。")
                                    else -> {
                                        val playerName = response.getDropdownResponse(1).elementContent
                                        val invitedPlayerData = WGuildModule.wguildPlayerConfig.safeGetData(playerName)
                                        if (invitedPlayerData.playerJoinGuildId == "") {
                                            targetWGuildData.guildInvitedPlayers[playerName] = player.name
                                            invitedPlayerData.receivedInvite[targetWGuildId] = player.name
                                            WGuildModule.wguildPlayerConfig.save()
                                            WGuildModule.wguildConfig.save()
                                            player.sendMsgWithTitle("&e操作成功! 您向名为 $playerName 的玩家发送了邀请.")
                                            WGuildModule.getModuleInfo().moduleOwner.server.getPlayer(playerName)?.sendMsgAndScreenTitle(
                                                    "&e&l你收到了一封来自 &c&l${player.name} &e&l的邀请, 邀请您加入公会 &b&l${targetWGuildData.guildDisplayName}",
                                                    "&c&l> &r&e你被邀请加入 &b&l${targetWGuildData.guildDisplayName} &r&c&l<",
                                                    "&7输入 &l/wgd g &r&7打开公会主面板以查看邀请请求")
                                        } else {
                                            player.sendMsgWithTitle("&e操作失败，对方已经加入其他公会。")
                                        }
                                    }
                                }

                            }

                            override fun onClosed(player: Player) {
                                player.showFormWindow(MyWGuildGUI(player))
                            }

                        }
                        player.showFormWindow(invitePlayerGUI)
                    } else {
                        player.sendMsgWithTitle("&c&l公会成员已满")
                    }
                }
            }

            if (targetPlayerPos.canPermitInvite) {
                addButton("查看申请列表") { player ->
                    val viewApplicationGUI = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&a申请列表".color()) {

                        init {
                            targetWGuildData.guildAskJoinPlayersName.forEach { applicationPlayerName ->
                                addButton(applicationPlayerName) { p ->
                                    val confirmGUI = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&a同意申请?".color(), "&a玩家 &c&l${applicationPlayerName} &r&e请求加入公会".color()) {

                                        init {
                                            val playerData = WGuildModule.wguildPlayerConfig.safeGetData(applicationPlayerName)
                                            addButton("&a&l同意".color()) { p ->
                                                if (playerData.playerJoinGuildId == "") {
                                                    targetWGuildData.guildAskJoinPlayersName.remove(applicationPlayerName)
                                                    targetWGuildData.joinPlayer(applicationPlayerName, targetWGuildId)
                                                } else {
                                                    p.sendMsgWithTitle("&c该玩家已经存在与另一个公会之中")
                                                }
                                            }
                                            addButton("&c&l拒绝".color()) { _ ->
                                                targetWGuildData.guildAskJoinPlayersName.remove(applicationPlayerName)
                                            }
                                            addButton("&7让我再想想...".color())
                                        }

                                        override fun onClosed(player: Player) {
                                            player.showFormWindow(MyWGuildGUI(player))
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

                if (targetPlayerPos.canKick) {
                    addButton("踢出玩家") { player ->
                        val kickPlayerView = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&a踢出玩家".color()) {

                            init {
                                addElement(ElementDropdown("&l&c选择你的要踢出玩家".color(), targetWGuildData.guildPlayersData.keys.toMutableList().also {
                                    it.add(0, "请选择")
                                    it.remove(player.name)
                                }, 0))
                            }

                            override fun onClicked(response: FormResponseCustom, player: Player) {
                                when (val kickTargetName = response.getDropdownResponse(0).elementContent.toString()) {
                                    "请选择" -> player.sendMsgWithTitle("&c&l操作失败，您未选择要踢出的玩家。")
                                    else -> {
                                        val kickedPlayerPos = targetWGuildData.getPlayerPosition(kickTargetName)
                                        if (!kickedPlayerPos.canKick) {
                                            targetWGuildData.kickPlayer(kickTargetName)
                                            player.sendMsgWithTitle("&a&l成功踢出玩家")
                                        } else {
                                            player.sendMsgWithTitle("&c&l你无法踢出同样拥有踢出权限的公会成员！")
                                        }
                                    }
                                }
                            }

                            override fun onClosed(player: Player) {
                                player.showFormWindow(MyWGuildGUI(player))
                            }

                        }

                        player.showFormWindow(kickPlayerView)

                    }
                }

                if (targetPlayerPos.isOwner && targetWGuildData.getLevelAttribute().canChangePositionSetting) {
                    addButton("修改职位设置") { player ->
                        player.showFormWindow(PresetPositionGUI(guildId = targetWGuildId))
                    }
                }

                if (targetPlayerPos.canManageMoney) {
                    addButton("升级公会") { player ->

                        val guildNextLevelData = targetWGuildData.getNextLevelAttribute()

                        val upgradeConfirmView = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&c确认升级公会？".color(), "&a&l这将会花费公会钱包的资金, 需要:&e${guildNextLevelData.price} &a来进行升级, 当前公会拥有 &e${targetWGuildData.guildMoney} &a资金".color()) {

                            init {
                                addButton("&a&l确认".color()) { p ->
                                    targetWGuildData.upgradeGuild(p)
                                }
                                addButton("&c&l取消".color())
                            }

                            override fun onClosed(player: Player) {
                                player.showFormWindow(MyWGuildGUI(player))
                            }

                        }

                        player.showFormWindow(upgradeConfirmView)

                    }
                }

            }

            if (!targetPlayerPos.isOwner) {
                addButton("退出公会") { player ->

                    val leaveConfirmView = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&c确认退出公会？".color(), "&c&l你退出公会将会扣除你贡献的所有活跃度，贡献资金不予退还".color()) {

                        init {
                            val playerData = WGuildModule.wguildPlayerConfig.safeGetData(player.name)
                            addButton("&a&l确认".color()) { p ->
                                if (playerData.playerJoinGuildId != "") {
                                    targetWGuildData.kickPlayer(player.name)
                                    p.sendMsgWithTitle("&a成功退出了公会")
                                } else {
                                    p.sendMsgWithTitle("&c你已经退出了这个公会，无法继续退出")
                                }
                            }
                            addButton("&c&l取消".color())
                        }

                        override fun onClosed(player: Player) {
                            player.showFormWindow(MyWGuildGUI(player))
                        }

                    }


                    player.showFormWindow(leaveConfirmView)
                }
            }

            if (targetPlayerPos.canDisband) {
                addButton("&c&l解散公会".color()) { player ->

                    val disbandConfirmView = object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&c确认解散公会？".color(), "&c&l解散公会后，资金将不会退还，一切将不可逆转".color()) {

                        init {
                            val playerData = WGuildModule.wguildPlayerConfig.safeGetData(player.name)
                            addButton("&a&l确认".color()) { p ->
                                if (playerData.playerJoinGuildId != "") {
                                    targetWGuildData.disbandGuild(p, targetWGuildId)
                                    p.sendMsgWithTitle("&a成功解散了公会")
                                } else {
                                    p.sendMsgWithTitle("&c你已经解散了这个公会，无法继续解散")
                                }
                            }
                            addButton("&c&l取消".color())
                        }

                        override fun onClosed(player: Player) {
                            player.showFormWindow(MyWGuildGUI(player))
                        }

                    }


                    player.showFormWindow(disbandConfirmView)
                }
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

    override fun onClosed(player: Player) {
        player.showFormWindow(MyWGuildGUI(player))
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
                    "&r&7捐献资金:&e${it.donatedMoney}\n".color() +
                    "&r&7最后一次签到:&e${it.signInData.toNormalData()}".color()
        }

    }

    override fun onClosed(player: Player) {
        player.showFormWindow(getWGuildData(player)?.let { MyWGuildPlayerList(it.value) })
    }

}