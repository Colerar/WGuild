package me.hbj233.wguild.gui

import cn.nukkit.Player
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.getRandomString
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.gui.ConfigGUI
import top.wetabq.easyapi.utils.color

object ManageServerWGuildGUI : ResponsibleFormWindowSimple("${WGuildPlugin.title}&e管理服务器公会".color(), "") {

    init {
        this.addButton("设置单个公会") { p ->
            p.showFormWindow(object : ResponsibleFormWindowSimple("${WGuildPlugin.title}&e设置单个公会".color(), "") {
                init {
                    WGuildModule.wguildConfig.simpleConfig.forEach {
                        addButton(it.key) { player ->
                            val configGUI = ConfigGUI(
                                    simpleCodecEasyConfig = WGuildModule.wguildConfig,
                                    obj = WGuildModule.wguildConfig.safeGetData(it.key),
                                    key = it.key,
                                    guiTitle = "${WGuildPlugin.title}&e设置 ${it.key} 公会".color(),
                                    parent = this
                            )
                            configGUI.setTranslateMap()
                            configGUI.init()
                            player.showFormWindow(configGUI)
                        }
                    }
                }

                private fun ConfigGUI<WGuildData>.setTranslateMap() {
                    this.setTranslateMap(
                            linkedMapOf(
                                    "guildDisplayName" to "公会显示名称",
                                    "guildDescription" to "公会介绍",
                                    "isVisible" to "公会对外可见",
                                    "usingPositionSettings" to "公会正在使用的职位设置",
                                    "usingSettings" to "公会正使用的配置",
                                    "canPVP" to "公会内部是否可以PVP",
                                    "signInAward" to "公会签到奖励",

                                    "guildLevel" to "公会等级",
                                    "guildMaxMember" to "公会最大人数",
                                    "guildNowMember" to "公会目前人数",
                                    "guildMoney" to "公会钱包",
                                    "guildActivity" to "公会活跃度",
                                    "createDate" to "%NONE%",
                                    "guildPlayersData" to "%NONE%",
                                    "guildInvitedPlayers" to "%NONE%",
                                    "guildAskJoinPlayersName" to "%NONE%"
                            )
                    )
                }

                override fun onClosed(player: Player) {
                    player.showFormWindow(ManageServerWGuildGUI)
                }
            })
        }

        this.addButton("解散单个公会") { p ->

        }

        this.addButton("设置公会预设") { p ->
            p.showFormWindow(object : ResponsibleFormWindowSimple
            ("${WGuildPlugin.title}&e设置公会预设".color(), "&e请您选择一个配置以修改或新建一个。 请注意：推荐直接修改等级以更改其他属性。".color()) {
                init {
                    this.addButton("新增配置") { p ->
                        p.showFormWindow(PresetSettingsGUI(
                                player = p,
                                key = "配置-${getRandomString()}",
                                title = "${WGuildPlugin.title}&e新增预设配置".color(),
                                isNew = true
                        ))
                    }

                    WGuildModule.wguildSettingsConfig.simpleConfig.forEach {
                        this.addButton(it.key) { p ->
                            p.showFormWindow(PresetSettingsGUI(
                                    player = p,
                                    key = it.key,
                                    title = "${WGuildPlugin.title}${it.key} &e预设的配置界面".color(),
                                    isNew = false
                            ))
                        }
                    }

                }

                override fun onClosed(player: Player) {
                    player.showFormWindow(WGuildMainGUI(player))
                }

            })
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(WGuildMainGUI(player))
    }

}

class PresetSettingsGUI(player: Player, key: String, title: String, isNew: Boolean) : ResponsibleFormWindowSimple(title.color(), "") {
    init {
        val isKeyContain = WGuildModule.wguildSettingsConfig.simpleConfig.containsKey(key)
        if (!isKeyContain && isNew || !isNew && isKeyContain) {
            //this.addButton("公会预设") { p -> p.showFormWindow(PresetCommonSettings("${WGuildPlugin.title}公会预设设置")) }

            WGuildModule.wguildSettingsConfig.safeGetData(key)
            WGuildModule.wguildSettingsConfig.save()

            this.addButton("等级预设") { p ->
                p.showFormWindow(PresetLevelGUI())
            }

        } else if (isKeyContain && isNew) {
            player.sendMsgWithTitle("&c设置失败! 原因: 与现有配置ID重复.")
        } else if (!isNew && !isKeyContain) {
            player.sendMsgWithTitle("&c设置失败! 原因: 配置ID不存在.")
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(ManageServerWGuildGUI)
    }
}

/*class PresetCommonSettings(title: String) : ResponsibleFormWindowCustom(title.color()) {
    init {
        addElement(ElementInput("设置ID:", "请输入"))
        addElement(ElementToggle("是否设置为玩家创建公会时的默认配置(这会替代原来的默认配置), 重启后生效."))
        addElement(ElementDropdown("公会等级组", WGuildModule.wguildSettingsConfig.simpleConfig.keys.toList()))
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val targetConfig = WGuildModule.wguildSettingsConfig.simpleConfig
        val inputResponseContent = response.getInputResponse(0)
        if (!targetConfig.containsKey(inputResponseContent)) {
            targetConfig[inputResponseContent] = WGuildSettingData(
                    response.getToggleResponse(0),
                    response.getDropdownResponse(1).elementContent,
                    response.getDropdownResponse(2).elementContent
            )
        }
        WGuildModule.wguildSettingsConfig.save()
    }
}*/