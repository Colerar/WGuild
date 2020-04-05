package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementInput
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.getRandomString
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.utils.color

class WGuildMainGUI(player: Player) : ResponsibleFormWindowSimple(
        "${WGuildPlugin.title}主面板".color(),
        "&e&l您需要进行什么操作？".color()
) {

    init {
        var isJoined = false
        val playerData = WGuildModule.wguildPlayerConfig.safeGetData(player.name)

        playerData.playerJoinGuildId
                .takeIf { it != "" }
                ?.let {
                    addButton("我的公会") { p -> p.showFormWindow(MyWGuildGUI(player)) }
                    isJoined = true
                }

        if (!isJoined) {
            if (playerData.receivedInvite.isNotEmpty()) {
                addButton("查看邀请 ${playerData.takeIf { it.receivedInvite.isNotEmpty() }?.run { "&c&l(有邀请!)" } ?: ""}".color()) { p -> p.showFormWindow(InviteGuildGUI(playerData)) }
            }
            addButton("创建公会") { p ->
                run {
                    val newWGuildGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&e创建公会") {

                        init {
                            parent = this@WGuildMainGUI
                            addElement(ElementInput("公会ID(之后不可修改， 用作系统内部处理)", "请输入"))
                            addElement(ElementInput("公会名称(用作显示)"))
                            addElement(ElementInput("公会介绍"))
                            if (WGuildModule.defaultSettingPair.third.canVisible) addElement(ElementToggle("公会是否公开(只可通过邀请进入)"))
                        }

                        override fun onClicked(response: FormResponseCustom, player: Player) {
                            val guildId = when (val input = response.getInputResponse(0)) {
                                "请输入", "", "SYSTEM" -> {
                                    "wguild-${getRandomString()}"
                                }
                                else -> {
                                    input
                                }
                            }
                            if (!WGuildModule.wguildConfig.simpleConfig.containsKey(guildId)) {
                                WGuildModule.wguildConfig.simpleConfig[guildId] = WGuildModule.wguildConfig.getDefaultValue()
                                WGuildModule.wguildConfig.simpleConfig[guildId]?.let { guildData ->
                                    if (guildData.editGuildInfo(
                                                    player = player,
                                                    guildDisplayName = response.getInputResponse(1),
                                                    guildDescription = response.getInputResponse(2),
                                                    isVisible = if (WGuildModule.defaultSettingPair.third.canVisible) response.getToggleResponse(3) else guildData.isVisible,
                                                    isFirst = true
                                            )) {
                                        guildData.joinPlayer(player.name, guildId, guildData.getPositionsGroup().filter { it.value.isOwner }.keys.first(), false)
                                        player.sendMsgWithTitle("&a&l公会创建成功。".color())
                                    } else {
                                        WGuildModule.wguildConfig.simpleConfig.remove(guildId)
                                        player.sendMsgWithTitle("&c公会创建失败。".color())
                                    }
                                }
                                WGuildModule.wguildConfig.save()
                            } else {
                                player.sendMsgWithTitle("&c操作失败， 公会ID ($guildId) 已经存在")
                            }

                        }

                        override fun onClosed(player: Player) {
                            goBack(player)
                        }

                    }
                    p.showFormWindow(newWGuildGUI)
                }
            }
            addButton("加入公会") { p -> p.showFormWindow(WGuildJoinGUI()) }
        }

        if (player.isOp) {
            addButton("管理服务器公会") { p -> run { if (p.isOp) p.showFormWindow(ManageServerWGuildGUI) } }
        }

    }
}