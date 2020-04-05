package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.*
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildLevelData
import me.hbj233.wguild.data.WGuildLevelsGroupData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.utils.color

class PresetLevelGUI(title: String = "${WGuildPlugin.title}&e等级设置面版") : ResponsibleFormWindowSimple(title.color()) {

    init {

        addButton("设置等级组") { p ->
            p.showFormWindow(LevelsGroupGUI())
        }

        addButton("设置服务器使用的默认公会配置") { player ->
            player.showFormWindow(ChooseGuildSettingGUI())
        }

    }


    override fun onClosed(player: Player) {
        player.showFormWindow(ManageServerWGuildGUI)
    }

}

class ChooseGuildSettingGUI(title: String = "${WGuildPlugin.title}&e设置服务器使用的默认公会配置") : ResponsibleFormWindowCustom(title.color()) {

    init {

        addElement(ElementLabel("&e请确保设置的配置，等级是连续的，形如：&c1, 2, 3, 4 &e这样连续的等级, 并且等级从 0 开始".color()))
        addElement(ElementDropdown("请选择配置.", WGuildModule.wguildSettingsConfig.simpleConfig.keys.toMutableList()))

    }


    override fun onClicked(response: FormResponseCustom, player: Player) {

        val dataName = response.getDropdownResponse(1).elementContent
        val settingData = WGuildModule.wguildSettingsConfig.safeGetData(dataName)
        var i = 0
        if (settingData.guildLevelsSetting.levelsGroup.keys.fold(true) { acc, it ->
                    if (it.toInt() == i && acc) {
                        i++
                        true
                    } else {
                        false
                    }
                }) {
            WGuildModule.wguildSettingsConfig.simpleConfig.values.firstOrNull { it.isDefaultSetting }?.isDefaultSetting = false
            WGuildModule.wguildSettingsConfig.safeGetData(dataName).isDefaultSetting = true
            WGuildModule.wguildSettingsConfig.save()
            player.sendMsgWithTitle("&a设置成功")
        } else {
            player.sendMsgWithTitle("&c你选择的配置等级不从 0 开始， 或者等级之间不连续，因此无法设置")
        }

    }

}


class LevelsGroupGUI(title: String = "${WGuildPlugin.title}&e设置等级组") : ResponsibleFormWindowCustom(title.color()) {

    init {
        addElement(ElementLabel("&e请按照提示进行设置！注意： 你应事先设置足够的单个等级。 您可以选择一个等级组以修改， 或选择\"新建配置\"同时填写ID，以新建。 设置的等级必须是连续的！".color()))
        addElement(ElementDropdown(
                "请选择配置", WGuildModule.wguildSettingsConfig.simpleConfig.keys.toMutableList()
                .also {
                    it.add(0, "请选择")
                    it.add(1, "新建")
                })

        )
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val dataName = response.getDropdownResponse(1).elementContent
        player.showFormWindow(ShowAllLevelGUI(dataName, player))
    }
}

class ShowAllLevelGUI(val dataName: String, player: Player, extra: String = "") : ResponsibleFormWindowSimple("${WGuildPlugin.title}&e等级组设置", "若您不小心多选择了需要创建/修改的等级数量， 不选择系统就会忽视它。 请注意， 等级的先后顺序就是他的升级顺序。 \n $extra".color()) {

    init {

        when (dataName) {
            "请选择" -> player.showFormWindow(
                    object : ResponsibleFormWindowSimple(WGuildPlugin.title, "&c&l您未选择,你可以点击继续以重新选择, 或直接退出") {
                        init {
                            addButton("继续") { player ->
                                player.showFormWindow(PresetLevelGUI())
                            }
                        }
                    }
            )

            else -> {
                val targetLevelGroupData = WGuildModule.wguildSettingsConfig.safeGetData(dataName)
                targetLevelGroupData.guildLevelsSetting.levelsGroup.keys.toMutableList()
                        .also { it.add("新建") }
                        .forEach { positionId ->
                            addButton(positionId) { p ->
                                p.showFormWindow(SingleLevelGUI(targetLevelGroupData.guildLevelsSetting, positionId, this))
                            }
                        }
            }
        }

    }

    override fun onClosed(player: Player) {
        player.showFormWindow(PresetLevelGUI())
    }
}


class SingleLevelGUI(
        private val levelsData: WGuildLevelsGroupData,
        level: String,
        private val p: ShowAllLevelGUI) : ResponsibleFormWindowCustom("${WGuildPlugin.title}&e设置单个等级") {

    private var hasChanged = false

    init {
        parent = p
        addElement(ElementSlider("等级", 1F, 100F, 1))
        val targetLevelData = levelsData.levelsGroup[level]
        if (targetLevelData is WGuildLevelData) {
            addElement(ElementSlider("最大人数", 1F, 500F, 5, targetLevelData.maxMembers.toFloat()))
            addElement(ElementToggle("能否修改公会职位设置", targetLevelData.canChangePositionSetting))
            addElement(ElementToggle("能否修改公会名", targetLevelData.canChangeName))
            addElement(ElementToggle("能否使用彩色公会名", targetLevelData.canUseColor))
            addElement(ElementToggle("能否公开展示公会", targetLevelData.canVisible))
            addElement(ElementToggle("能否开启公会内PVP", targetLevelData.canPVP))
            addElement(ElementInput("升级需要的资金", "输入升级需要的资金", targetLevelData.price.toString()))
        } else {
            addElement(ElementSlider("最大人数", 1F, 500F, 5, 5F))
            addElement(ElementToggle("能否修改公会职位设置", false))
            addElement(ElementToggle("能否修改公会名", true))
            addElement(ElementToggle("能否使用彩色公会名", false))
            addElement(ElementToggle("能否公开展示公会", false))
            addElement(ElementToggle("能否开启公会内PVP", false))
            addElement(ElementInput("升级需要的资金", "输入升级需要的资金"))
        }
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val level = response.getSliderResponse(0).toInt()
        val maxMembers = response.getSliderResponse(1).toInt()
        val canChangePositionSetting = response.getToggleResponse(2)
        val canChangeName = response.getToggleResponse(3)
        val canUseColor = response.getToggleResponse(4)
        val canVisible = response.getToggleResponse(5)
        val canPVP = response.getToggleResponse(6)
        val price = response.getInputResponse(7).toInt()
        val newLevelData = WGuildLevelData(maxMembers, canChangePositionSetting, canChangeName, canUseColor, canVisible, canPVP, price)
        levelsData.levelsGroup[level.toString()] = newLevelData
        WGuildModule.wguildSettingsConfig.save()
        hasChanged = true
        player.showFormWindow(ShowAllLevelGUI(p.dataName, player, "&a&l修改成功，选择下面其中一项继续修改"))
    }

    override fun onClosed(player: Player) {
        if (hasChanged) {
            player.showFormWindow(ShowAllLevelGUI(p.dataName, player, "&a&l修改成功，选择下面其中一项继续修改"))
        } else {
            goBack(player)
        }
    }

}