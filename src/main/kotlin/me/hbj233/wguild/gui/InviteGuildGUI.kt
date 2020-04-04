package me.hbj233.wguild.gui

import cn.nukkit.Player
import cn.nukkit.form.window.FormWindow
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.PlayerGuildData
import me.hbj233.wguild.data.WGuildData
import me.hbj233.wguild.module.WGuildModule
import me.hbj233.wguild.utils.sendMsgWithTitle
import moe.him188.gui.window.ResponsibleFormWindowSimple
import top.wetabq.easyapi.utils.color

class InviteGuildGUI(playerData: PlayerGuildData) : ResponsibleFormWindowSimple(
        "${WGuildPlugin.title}&e邀请列表".color(),
        "&e&l你可以在此看到所有的邀请信息"
) {

    init {
        playerData.receivedInvite.forEach { (guildId, inviter) ->
            val targetGuildData = WGuildModule.wguildConfig.safeGetData(guildId)
            addButton("&c&l$inviter &r&e邀请您加入公会 &b&l${targetGuildData.guildDisplayName}".color()) { p ->
                p.showFormWindow(ConfirmInviteGuildGUI(targetGuildData, playerData, inviter, guildId, this))
            }
        }
    }

    override fun onClosed(player: Player) {
        player.showFormWindow(WGuildMainGUI(player))
    }

}

class ConfirmInviteGuildGUI(targetGuildData: WGuildData, playerData: PlayerGuildData, inviter: String, guildId: String, parent: FormWindow): ResponsibleFormWindowSimple(
        "${WGuildPlugin.title}&e邀请信息".color(),
        "&c&l$inviter &r&e邀请您加入公会 &b&l${targetGuildData.guildDisplayName}".color()
) {

    init {
        setParent(parent)
        if (playerData.playerJoinGuildId == "") {
            addButton("&a&l同意") { p ->
                if (playerData.playerJoinGuildId == "") {
                    targetGuildData.guildInvitedPlayers.remove(p.name)
                    playerData.receivedInvite.remove(guildId)
                    targetGuildData.joinPlayer(p.name, guildId)
                } else {
                    p.sendMsgWithTitle("&c你已经加入了一个公会哦!")
                }
            }
            addButton("&c&l拒绝") { p ->
                targetGuildData.guildInvitedPlayers.remove(p.name)
                playerData.receivedInvite.remove(guildId)
            }
            addButton("&7让我再想想...")
        } else {
            addButton("&7你已经加入一个公会了, 不能贪心哦!")
        }

    }

    override fun onClosed(player: Player) {
        goBack(player)
    }

}