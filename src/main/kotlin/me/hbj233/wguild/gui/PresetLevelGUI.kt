package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementSlider
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildLevelData
import me.hbj233.wguild.data.WGuildLevelsGroupData
import me.hbj233.wguild.module.WGuildModule
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.gui.ConfigGUI
import top.wetabq.easyapi.utils.color

class PresetLevelGUI(title : String = "${WGuildPlugin.title} &e等级设置面版") : ResponsibleFormWindowSimple(title.color()) {

    init {
        this.addButton("设置等级组") { p ->
            p.showFormWindow(LevelsGroupGUI())
        }
        this.addButton("设置单个等级") { p ->
            p.showFormWindow(SingleLevelGUI())
        }
    }


    override fun onClosed(player: Player) {
        player.showFormWindow(ManageServerWGuildGUI)
    }

}


class LevelsGroupGUI(title: String = "${WGuildPlugin.title} &e设置等级组") : ResponsibleFormWindowCustom(title.color()) {

    init {
        addElement(ElementLabel("&e请按照提示进行设置!注意: 你应事先设置足够的单个等级. 您可以选择一个等级组以修改, 或选择\"新建配置\"同时填写ID,以新建. ".color()))
        addElement(ElementSlider("等级组包含的等级数量", 2F, 100F, 1, 5F))
        addElement(ElementDropdown(
                "请选择配置.", WGuildModule.wguildLevelsGroupConfig.simpleConfig.keys.toMutableList()
                .also {
                    it.add(0, "请选择")
                    it.add(1, "新建")
                })
        )
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {

        player.showFormWindow(
                object : ResponsibleFormWindowCustom("${WGuildPlugin.title} &e等级组设置") {
                    val levelNumber = response.getSliderResponse(1).toInt()
                    val dataName = response.getDropdownResponse(2).elementContent
                    val response1 = response

                    init {
                        addElement(ElementLabel("若您不小心多选择了需要创建/修改的等级数量, 不选择系统就会忽视它. 请注意, 等级的先后顺序就是他的升级顺序.".color()))

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
                                repeat(levelNumber) { it ->

                                    addElement(ElementDropdown("请选择等级组中的第${it + 1}个等级",
                                            WGuildModule.wguildLevelsConfig.simpleConfig.keys.toMutableList().also {
                                                it.add(0, "请选择")
                                            }))

                                }
                            }
                        }
                    }

                    override fun onClicked(response: FormResponseCustom, player: Player) {
                        val targetConfig = WGuildModule.wguildLevelsGroupConfig.simpleConfig
                        var targetLevelsData: WGuildLevelsGroupData? = null
                        if (dataName != "新建") {
                            targetConfig[dataName]?.let {
                                targetLevelsData = it
                            }
                        } else {
                            targetConfig.put(dataName, WGuildModule.wguildLevelsGroupConfig.getDefaultValue())?.let {
                                targetLevelsData = it
                            }
                        }
                        targetLevelsData?.levelsGroup?.clear()
                        repeat(levelNumber) {
                            targetLevelsData?.levelsGroup?.put(levelNumber.toString(), response1.getDropdownResponse(it).elementContent)
                        }
                        WGuildModule.wguildLevelsGroupConfig.save()
                    }

                    override fun onClosed(player: Player) {
                        player.showFormWindow(PresetLevelGUI())
                    }

                }
        )

    }

}



class SingleLevelGUI(title: String  = "${WGuildPlugin.title} &e设置单个等级") : ResponsibleFormWindowSimple(title.color(), "&e您可以新建一个等级或选择一个以修改") {

    init {
        addButton("新建配置") { player ->
            player.showFormWindow(getLevelsConfigGUI("新建配置"))
        }

        WGuildModule.wguildLevelsConfig.simpleConfig.keys.forEach {
            addButton(it) { player ->
                player.showFormWindow(getLevelsConfigGUI(it))
            }
        }

    }

    private fun ConfigGUI<WGuildLevelData>.setTranslateMap() {
        this.setTranslateMap(
                linkedMapOf(
                        "maxMembers" to "最大人数",
                        "canChangeName" to "能否改名",
                        "canUseColor" to "能否使用颜色符",
                        "price" to "价格"
                )
        )
    }

    private fun getLevelsConfigGUI(key : String): ConfigGUI<WGuildLevelData> {
        val obj = if (WGuildModule.wguildLevelsConfig.simpleConfig.containsKey(key)){
            WGuildModule.wguildLevelsConfig.safeGetData(key)
        } else {
            WGuildModule.wguildLevelsConfig.getDefaultValue()
        }
        val createLevelsGUI = ConfigGUI(
                WGuildModule.wguildLevelsConfig,
                obj, key,
                "${WGuildPlugin.title} &e $key 的等级配置面板".color()
        )
        createLevelsGUI.setTranslateMap()
        createLevelsGUI.init()
        return createLevelsGUI
    }

}