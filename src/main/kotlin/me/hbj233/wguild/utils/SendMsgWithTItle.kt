package me.hbj233.wguild.utils

import cn.nukkit.Player
import me.hbj233.wguild.WGuildPlugin
import top.wetabq.easyapi.utils.color

fun Player.sendMsgWithTitle(message: String) {
    this.sendMessage("${WGuildPlugin.title}$message".color())
}

fun Player.sendMsgAndScreenTitle(message: String, title: String, subTitle: String = "") {
    this.setSubtitle(subTitle.color())
    this.sendTitle(title.color())
    sendMsgWithTitle(message)
}