package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import cn.nukkit.form.window.FormWindow
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowCustom
import top.wetabq.easyapi.utils.color

class WGuildJoinGUI : ResponsibleFormWindowCustom(
        "${WGuildPlugin.title}&e加入公会".color()) {

    private val canJoinWGuildMap = linkedMapOf<String, String>()

    init {
        val targetConfig = WGuildModule.wguildConfig.simpleConfig
        targetConfig.forEach {
            if (it.value.isVisible) canJoinWGuildMap[it.key] = "${it.value.guildDisplayName} &r&e- &a${it.value.guildNowMember}&7/&c${it.value.guildMaxMember}".color()
        }
        addElement(ElementDropdown("请选择一个公会加入:", canJoinWGuildMap.values
                .toMutableList().also {
                    it.add(0, "请选择")
                })
        )
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val targetElementContent = canJoinWGuildMap.filter { it.value == response.getDropdownResponse(0).elementContent }.keys.first()
        if (targetElementContent != "请选择") {
            val targetWGuildData = WGuildModule.wguildConfig.safeGetData(targetElementContent)
            player.showFormWindow(WGuildJoinSubGUI(this, targetWGuildData))
        } else {
            player.sendMsgWithTitle("&c操作失败，请选择公会。")
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(WGuildMainGUI(player))
    }

}

class WGuildJoinSubGUI(parent: FormWindow, private val targetGuildData: WGuildData) : ResponsibleFormWindowCustom(
        "${WGuildPlugin.title}&e加入公会") {

    init {
        setParent(parent)
        addElement(ElementLabel(targetGuildData.getGuildInformation()))
        addElement(ElementToggle("确定加入？", false))
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        if (response.getToggleResponse(1)) {
            if (targetGuildData.guildNowMember + 1 <= targetGuildData.guildMaxMember) {
                targetGuildData.applyJoin(player.name)
            } else {
                player.sendMsgWithTitle("&c&l申请的公会已满，无法添加新的成员")
            }
        }
    }

    override fun onClosed(player: Player) {
        goBack(player)
    }
}