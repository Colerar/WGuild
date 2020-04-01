package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import cn.nukkit.form.window.FormWindow
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowCustom
import top.wetabq.easyapi.utils.color

object WGuildJoinGUI : ResponsibleFormWindowCustom(
        "${WGuildPlugin.title} &e加入公会".color()){

    private val canJoinWGuildMap = linkedMapOf<String, String>()

    init {
        val targetConfig = WGuildModule.wguildConfig.simpleConfig
        targetConfig.forEach {
            if (it.value.isVisible) canJoinWGuildMap[it.key] = it.value.guildDisplayName
        }
        addElement(ElementDropdown("请选择一个公会加入:", canJoinWGuildMap.values
                .toMutableList().also {
                    it.add(0, "请选择")
                })
        )
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val targetElementContent = canJoinWGuildMap[response.getDropdownResponse(0).elementContent]
        if (targetElementContent != "请选择"){
            targetElementContent?.let{
                val targetWGuildData = WGuildModule.wguildConfig.safeGetData(targetElementContent)
                player.showFormWindow(WGuildJoinSubGUI(this, targetElementContent))
            }
        } else {
            player.sendMsgWithTitle("&c操作失败, 请选择公会.")
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(WGuildMainGUI(player))
    }

}

class WGuildJoinSubGUI(parent: FormWindow, wguildKey : String) :ResponsibleFormWindowCustom(
        "${WGuildPlugin.title} &e加入公会") {
    private val targetData = WGuildModule.wguildConfig.safeGetData(wguildKey)
    init {
        setParent(parent)
        addElement(ElementLabel(targetData.getGuildInformation()))
        addElement(ElementToggle("确定加入?",false))
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        if (response.getToggleResponse(1)){
            val targetRequestLine = targetData.guildAskJoinPlayersName
            if (!targetRequestLine.contains(player.name)) {
                targetRequestLine.add(player.name)
                player.sendMsgWithTitle("&e申请成功!.")
            } else {
                player.sendMsgWithTitle("&c您已经申请过了.")
            }
        }
    }

    override fun onClosed(player: Player) {
        goBack(player)
    }
}