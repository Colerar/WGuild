package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.*
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildData
import me.hbj233.wguild.data.WGuildSettingData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.getRandomString
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.gui.ConfigGUI
import top.wetabq.easyapi.utils.color

object ManageServerWGuildGUI : ResponsibleFormWindowSimple("${WGuildPlugin.title} &e管理服务器公会".color(),"") {

    init {
        this.addButton("设置单个公会") { p ->
            p.showFormWindow(object : ResponsibleFormWindowSimple("${WGuildPlugin.title} &e设置单个公会","") {
                init {
                    WGuildModule.wguildConfig.simpleConfig.forEach {
                        addButton(it.key) { player ->
                            val configGUI = ConfigGUI(
                                    simpleCodecEasyConfig = WGuildModule.wguildConfig,
                                    obj = WGuildModule.wguildConfig.safeGetData(it.key),
                                    key = it.key,
                                    guiTitle = "${WGuildPlugin.title} &e设置 ${it.key} 公会",
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
                                    "guildLevel" to "公会等级",
                                    "guildDescription" to "公会介绍",
                                    "guildMaxMember" to "公会最大人数",
                                    "guildNowMember" to "%NONE%",
                                    "guildMoney" to "公会钱包",
                                    "createDate" to "%NONE%",
                                    "guildActivity" to "公会活跃度",
                                    "isVisible" to "公会对外可见",
                                    "canChangeUsingSettings" to "公会管理员可以修改本页面所包含的设置",
                                    "guildPlayersData" to "%NONE%",
                                    "guildInvitedPlayers" to "%NONE%",
                                    "guildAskJoinPlayersName" to "%NONE%",
                                    "usingSettings" to "公会正使用的预设"
                            )
                    )
                }
            })
        }
        this.addButton("设置公会预设") { p -> p.showFormWindow(object : ResponsibleFormWindowSimple
            ("${WGuildPlugin.title} &e设置公会预设".color(),"&e请您选择一个配置以修改或新建一个. 请注意: 推荐直接修改等级以更改其他属性.".color()) {
                init {
                    this.addButton("新增配置") { p ->
                        p.showFormWindow(PresetSettingsGUI(
                                player = p,
                                key = "配置-${getRandomString()}",
                                title = "${WGuildPlugin.title} &e新增预设配置".color(),
                                isNew = true
                        ))
                    }

                    WGuildModule.wguildSettingsConfig.simpleConfig.forEach {
                        this.addButton(it.key) { p ->
                            p.showFormWindow(PresetSettingsGUI(
                                    player = p,
                                    key = it.key,
                                    title = "${WGuildPlugin.title} ${it.key} &e预设的配置界面".color(),
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

class PresetSettingsGUI(player: Player,key: String, title: String, isNew : Boolean) : ResponsibleFormWindowSimple(title.color(),"") {
    init {
        val isKeyContain = WGuildModule.wguildSettingsConfig.simpleConfig.containsKey(key)
        if (!isKeyContain && isNew || !isNew && isKeyContain) {
            this.addButton("公会预设") { p -> p.showFormWindow(PresetCommonSettings("${WGuildPlugin.title}公会预设设置")) }

            this.addButton("成员权限预设") {
                p -> p.showFormWindow(PresetPositionGUI())
            }

            this.addButton("等级预设") {
                p -> p.showFormWindow(PresetLevelGUI())
            }

        } else if(isKeyContain && isNew){
            player.sendMessage("${WGuildPlugin.title} &c设置失败! 原因: 与现有配置ID重复.")
        } else if (!isNew && !isKeyContain) {
            player.sendMessage("${WGuildPlugin.title} &c设置失败! 原因: 配置ID不存在.")
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(ManageServerWGuildGUI)
    }
}

class PresetCommonSettings(title: String) : ResponsibleFormWindowCustom(title.color()) {
    init {
        addElement(ElementInput("设置ID:","请输入"))
        addElement(ElementToggle("是否设置为玩家创建公会时的默认配置(这会替代原来的默认配置), 重启后生效."))
        addElement(ElementDropdown("公会权限组", WGuildModule.wguildPositionsGroupsConfig.simpleConfig.keys.toList()))
        addElement(ElementDropdown("公会等级组", WGuildModule.wguildLevelsGroupConfig.simpleConfig.keys.toList()))
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val targetConfig = WGuildModule.wguildSettingsConfig.simpleConfig
        val inputResponseContent = response.getInputResponse(0)
        if (!targetConfig.containsKey(inputResponseContent)){
            targetConfig[inputResponseContent] = WGuildSettingData(
                    response.getToggleResponse(0),
                    response.getDropdownResponse(1).elementContent,
                    response.getDropdownResponse(2).elementContent
            )
        }
        WGuildModule.wguildSettingsConfig.save()
    }
}