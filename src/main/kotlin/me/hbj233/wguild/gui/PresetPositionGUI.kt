package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementSlider
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildPositionData
import me.hbj233.wguild.data.WGuildPositionsGroupData
import me.hbj233.wguild.module.WGuildModule
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.gui.ConfigGUI
import top.wetabq.easyapi.utils.color

class PresetPositionGUI(title: String = "${WGuildPlugin.title} &e成员权限预设面板".color()) : ResponsibleFormWindowSimple(title, "") {

    init {
        addButton("设置权限组") { player ->
            player.showFormWindow(PositionGroupGUI())
        }

        addButton("设置单个权限") { player ->
            player.showFormWindow(SinglePositionGUI())
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(ManageServerWGuildGUI)
    }
}

class PositionGroupGUI(title: String = "${WGuildPlugin.title} &e设置权限组") : ResponsibleFormWindowCustom(title.color()) {

    init {
        addElement(ElementLabel("&e请按照提示进行设置!注意: 你应事先设置足够的单个权限. 您可以选择一个权限组以修改, 或选择\"新建配置\"同时填写ID,以新建. ".color()))
        addElement(ElementSlider("权限组包含的权限数量", 2F, 100F, 1, 3F))
        addElement(ElementDropdown(
                "请选择配置.", WGuildModule.wguildPositionsGroupsConfig.simpleConfig.keys.toMutableList()
                .also {
                    it.add(0, "请选择")
                    it.add(1, "新建")
                })
        )
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val positionNumber = response.getSliderResponse(1).toInt()
        val dataName = response.getDropdownResponse(2).elementContent
        val response1 = response
        player.showFormWindow(
                object : ResponsibleFormWindowCustom("${WGuildPlugin.title} &e权限组设置") {



                    init {
                        addElement(ElementLabel("若您不小心多选择了需要创建/修改的权限数量, 不选择系统就会忽视它.".color()))

                        when (dataName) {
                            "请选择" -> player.showFormWindow(
                                    object : ResponsibleFormWindowSimple(WGuildPlugin.title, "&c&l您未选择,你可以点击继续以重新选择, 或直接退出") {
                                        init {
                                            addButton("继续") { player ->
                                                player.showFormWindow(PresetPositionGUI())
                                            }
                                        }
                                    }
                            )

                            else -> {
                                repeat(positionNumber) { it ->

                                    addElement(ElementDropdown("请选择第${it + 1}个权限",
                                            WGuildModule.wguildPositionsConfig.simpleConfig.keys.toMutableList().also {
                                                it.add(0, "请选择")
                                            }))

                                }
                            }
                        }
                    }

                    override fun onClicked(response: FormResponseCustom, player: Player) {
                        val targetConfig = WGuildModule.wguildPositionsGroupsConfig.simpleConfig
                        var targetPositionData: WGuildPositionsGroupData? = null
                        if (dataName != "新建") {
                            targetConfig[dataName]?.let {
                                targetPositionData = it
                            }
                        } else {
                            targetConfig.put(dataName, WGuildModule.wguildPositionsGroupsConfig.getDefaultValue())?.let {
                                targetPositionData = it
                            }
                        }
                        targetPositionData?.positionsGroup?.clear()
                        repeat(positionNumber) {
                            targetPositionData?.positionsGroup?.add(response1.getDropdownResponse(it).elementContent)
                        }

                        WGuildModule.wguildPositionsGroupsConfig.save()
                    }

                    override fun onClosed(player: Player) {
                        player.showFormWindow(PresetPositionGUI())
                    }

                }
        )

    }

}

class SinglePositionGUI(title: String  = "${WGuildPlugin.title} &e设置单个权限") : ResponsibleFormWindowSimple(title.color(), "&e您可以新建一个权限或选择一个以修改".color()) {

    init {
        addButton("新建配置") { player ->
            player.showFormWindow(getPositionConfigGUI("新建配置"))
        }

        WGuildModule.wguildPositionsConfig.simpleConfig.keys.forEach {
            addButton(it) { player ->
                player.showFormWindow(getPositionConfigGUI(it))
            }
        }

    }

    private fun ConfigGUI<WGuildPositionData>.setTranslateMap() {
        this.setTranslateMap(
            linkedMapOf(
                    "displayName" to "显示名称",
                    "isOwner" to "是否为公会长",
                    "canKick" to "能否踢人",
                    "canInvite" to "能否邀请",
                    "canPermitInvite" to "能否允许申请请",
                    "canChangeGuildName" to "可以更改公会名称",
                    "canDissolve" to "可以解散公会",
                    "canManageMoney" to "可以管理工会钱包"
            )
        )
    }

    private fun getPositionConfigGUI(key : String): ConfigGUI<WGuildPositionData> {
        val obj = if (WGuildModule.wguildPositionsConfig.simpleConfig.containsKey(key)){
            WGuildModule.wguildPositionsConfig.safeGetData(key)
        } else {
            WGuildModule.wguildPositionsConfig.getDefaultValue()
        }
        val createPositionGUI = ConfigGUI(
                WGuildModule.wguildPositionsConfig,
                obj, key,
                "${WGuildPlugin.title} &e $key 的权限配置面板".color()
        )
        createPositionGUI.setTranslateMap()
        createPositionGUI.init()
        return createPositionGUI
    }

}