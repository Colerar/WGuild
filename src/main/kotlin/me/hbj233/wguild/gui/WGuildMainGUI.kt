package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.Element
import cn.nukkit.form.element.ElementInput
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildPlayerData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.getRandomString
import me.hbj233.wguild.utils.getTodayDate
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.gui.ConfigGUI
import top.wetabq.easyapi.utils.color

class WGuildMainGUI(player: Player) : ResponsibleFormWindowSimple(
        "${WGuildPlugin.title} 主面板".color(),
        "&e&l您需要进行什么操作?".color()
) {

    init {
        var isJoined = false

        WGuildModule.wguildConfig.simpleConfig.values.forEach {
            if (it.guildPlayersData.keys.contains(player.name)){
                addButton("我的公会") { p -> p.showFormWindow(MyWGuildGUI(player)) }
                isJoined = true
            }
        }

        if (!isJoined) {
            addButton("创建公会") { p ->
                run {
                    val newWGuildGUI = object : ResponsibleFormWindowCustom("${WGuildPlugin.title}&e创建公会") {

                        init {
                            addElement(ElementInput("公会ID(之后不可修改, 用作系统内部处理)","请输入","请输入"))
                            addElement(ElementInput("公会名称(用作显示)"))
                            addElement(ElementInput("公会介绍"))
                            addElement(ElementToggle("公会是否公开(只可通过邀请进入)"))
                        }

                        override fun onClicked(response: FormResponseCustom, player: Player) {
                            val guildId = when (val input = response.getInputResponse(0)){
                                "请输入" -> {
                                    "wguild-${getRandomString()}"
                                }
                                else -> {
                                    input
                                }
                            }
                            if (!WGuildModule.wguildConfig.simpleConfig.containsKey(guildId)){
                                val targetConfig = WGuildModule.wguildConfig.simpleConfig.put(guildId, WGuildModule.wguildConfig.getDefaultValue())
                                WGuildModule.wguildConfig.simpleConfig[guildId]?.let { it ->

                                    it.guildDisplayName = response.getInputResponse(1)
                                    it.guildDescription = response.getInputResponse(2)
                                    it.isVisible = response.getToggleResponse(3)

                                    val positionsGroupDataName = WGuildModule.wguildSettingsConfig.simpleConfig[it.usingSettings]?.guildPositions
                                    var positionName : String? = null

                                    val positions = WGuildModule.wguildPositionsGroupsConfig.simpleConfig[positionsGroupDataName]?.positionsGroup
                                    if (!positions.isNullOrEmpty()){
                                        positions.forEach {
                                            if (WGuildModule.wguildPositionsConfig.simpleConfig[it]?.isOwner == true) {
                                                positionName = it
                                            }
                                        }
                                        positionName?.let { it2 ->
                                            it.guildPlayersData.put(player.name, WGuildPlayerData(it2, getTodayDate(), 0.0, 0))
                                        }
                                    } else {
                                        player.sendMsgWithTitle("&c创建失败, 请联系管理员, 错误原因: 公会权限组不存在.".color())
                                        WGuildModule.wguildConfig.simpleConfig.remove(guildId)
                                    }

                                }
                                WGuildModule.wguildConfig.save()
                            } else {
                                player.sendMsgWithTitle("&c操作失败, 公会ID ($guildId) 已经存在")
                            }

                        }

                    }
                    p.showFormWindow(newWGuildGUI)
                }
            }
            addButton("加入公会") { p -> p.showFormWindow(WGuildJoinGUI) }
        }

        if (player.isOp) {
            addButton("管理服务器公会") { p -> run { if (p.isOp) p.showFormWindow(ManageServerWGuildGUI) } }
        }

    }
}