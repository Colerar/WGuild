package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.element.ElementInput
import cn.nukkit.form.element.ElementLabel
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.WGuildPositionData
import me.hbj233.wguild.data.WGuildPositionsGroupData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowCustom
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.utils.color

class PresetPositionGUI(title: String = "${WGuildPlugin.title}&e成员权限预设面板".color(), guildId: String) : ResponsibleFormWindowSimple(title, "") {

    init {
        addButton("设置职位权限配置") { player ->
            player.showFormWindow(PositionGroupGUI(guildId = guildId))
        }
        if (WGuildModule.wguildConfig.safeGetData(guildId).getLevelAttribute().canChangePositionSetting) {
            addButton("设置公会使用的职位权限配置") { player ->
                player.showFormWindow(ChooseGuildPositionGUI(guildId = guildId))
            }
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(MyWGuildGUI(player))
    }
}

class ChooseGuildPositionGUI(title: String = "${WGuildPlugin.title}&e设置公会职位权限配置", private val guildId: String) : ResponsibleFormWindowCustom(title.color()) {

    init {

        addElement(ElementLabel("&e请确保设置的配置，有一个会长职位，有一个成员默认职位".color()))
        addElement(ElementDropdown("请选择配置", WGuildModule.wguildPositionsGroupsConfig.simpleConfig.keys.toMutableList()))

    }


    override fun onClicked(response: FormResponseCustom, player: Player) {

        val dataName = response.getDropdownResponse(1).elementContent
        val groupData = WGuildModule.wguildPositionsGroupsConfig.safeGetData(dataName)
        if (groupData.positionsGroup.values.firstOrNull { it.isOwner } != null && groupData.positionsGroup.values.firstOrNull { it.isDefault } != null) {
            WGuildModule.wguildConfig.safeGetData(guildId).usingPositionSettings = dataName
            WGuildModule.wguildConfig.save()
        } else {
            player.sendMsgWithTitle("&c你选择的配置没有包含会长和成员的默认权限，因此无法设置")
        }

    }

}

class PositionGroupGUI(title: String = "${WGuildPlugin.title}&e设置职位权限", private val guildId: String) : ResponsibleFormWindowCustom(title.color()) {

    init {
        addElement(ElementLabel("&e请按照提示进行设置！注意： 您可以选择一个你自己创建的职位权限以修改， 或选择\"新建配置\"同时填写ID，以新建。 \n &c请注意职位权限配置的拥有者，&l当你不是该配置的拥有者时，你只有对该职位权限使用的权限，而没有编辑权限".color()))
        addElement(ElementDropdown(
                "请选择配置", WGuildModule.wguildPositionsGroupsConfig.simpleConfig.keys.toMutableList()
                .also {
                    it.add(0, "请选择")
                    it.add(1, "新建")
                })
        )
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val dataName = response.getDropdownResponse(1).elementContent
        player.showFormWindow(ShowAllPositionGUI(dataName, player, guildId))

    }

}

class ShowAllPositionGUI(val dataName: String, player: Player, val guildId: String, extra: String = "") : ResponsibleFormWindowSimple("${WGuildPlugin.title}&e职位权限设置", "&e选择你需要需改的职位\n $extra".color()) {

    init {

        when (dataName) {
            "请选择" -> player.showFormWindow(
                    object : ResponsibleFormWindowSimple(WGuildPlugin.title, "&c&l您未选择,你可以点击继续以重新选择, 或直接退出") {
                        init {
                            addButton("继续") { player ->
                                player.showFormWindow(PresetPositionGUI(guildId = guildId))
                            }
                        }
                    }
            )

            else -> {
                val targetPermissionGroupData = WGuildModule.wguildPositionsGroupsConfig.safeGetData(dataName)
                if (targetPermissionGroupData.ownerGuild == guildId) {
                    targetPermissionGroupData.positionsGroup.keys.toMutableList()
                            .also { it.add("新建") }
                            .forEach { positionId ->
                                addButton(positionId) { p ->
                                    p.showFormWindow(SinglePositionGUI(targetPermissionGroupData, positionId, this))
                                }
                            }
                } else {
                    player.showFormWindow(
                            object : ResponsibleFormWindowSimple(WGuildPlugin.title, "&c&l您无法修改这个职位权限,你可以点击继续以重新选择, 或直接退出") {
                                init {
                                    addButton("继续") { player ->
                                        player.showFormWindow(PresetPositionGUI(guildId = guildId))
                                    }
                                }
                            }
                    )
                }
            }
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(PresetPositionGUI(guildId = guildId))
    }

}

class SinglePositionGUI(
        private val positionGroupsData: WGuildPositionsGroupData,
        id: String,
        private val p: ShowAllPositionGUI) : ResponsibleFormWindowCustom("${WGuildPlugin.title}&e设置职位权限") {

    private var hasChanged = false

    init {
        parent = p
        addElement(ElementInput("职位Id", id, id))
        val targetPositionData = positionGroupsData.positionsGroup[id]
        if (targetPositionData is WGuildPositionData) {
            addElement(ElementInput("显示名称", "输入职位的显示名称", targetPositionData.displayName))
            addElement(ElementToggle("是否为会长权限（整个权限组必须有一个会长职位）", targetPositionData.isOwner))
            addElement(ElementToggle("能否踢人", targetPositionData.canKick))
            addElement(ElementToggle("能否邀请其他玩家", targetPositionData.canInvite))
            addElement(ElementToggle("能否审核邀请请求", targetPositionData.canPermitInvite))
            addElement(ElementToggle("能否修改公会设置", targetPositionData.canChangeSetting))
            addElement(ElementToggle("能否解散公会", targetPositionData.canDisband))
            addElement(ElementToggle("能否管理公会资金", targetPositionData.canManageMoney))
            addElement(ElementToggle("是否为默认（整个权限组必须有一个默认职位）", targetPositionData.isDefault))
        } else {
            addElement(ElementInput("显示名称", "输入职位的显示名称"))
            addElement(ElementToggle("是否为会长权限（整个权限组必须有一个会长职位）", false))
            addElement(ElementToggle("能否踢人", false))
            addElement(ElementToggle("能否邀请其他玩家", false))
            addElement(ElementToggle("能否审核邀请请求", false))
            addElement(ElementToggle("能否修改公会设置", false))
            addElement(ElementToggle("能否解散公会", false))
            addElement(ElementToggle("能否管理公会资金", false))
            addElement(ElementToggle("是否为默认（整个权限组必须有一个默认职位）", false))
        }
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val id = response.getInputResponse(0)
        val displayName = response.getInputResponse(1)
        val isOwner = response.getToggleResponse(2)
        val canKick = response.getToggleResponse(3)
        val canInvite = response.getToggleResponse(4)
        val canPermitInvite = response.getToggleResponse(5)
        val canChangeSetting = response.getToggleResponse(6)
        val canDisband = response.getToggleResponse(7)
        val canManageMoney = response.getToggleResponse(8)
        val isDefault = response.getToggleResponse(9)
        val newPositionData = WGuildPositionData(displayName, isOwner, canKick, canInvite, canPermitInvite, canChangeSetting, canDisband, canManageMoney, isDefault)
        positionGroupsData.positionsGroup[id] = newPositionData
        WGuildModule.wguildPositionsGroupsConfig.save()
        hasChanged = true
    }

    override fun onClosed(player: Player) {
        if (hasChanged) {
            player.showFormWindow(ShowAllPositionGUI(p.dataName, player, p.guildId, "&a&l修改成功，选择下面其中一项继续修改"))
        } else {
            goBack(player)
        }
    }

}