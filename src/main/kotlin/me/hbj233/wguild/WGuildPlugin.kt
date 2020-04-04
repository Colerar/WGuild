package me.hbj233.wguild

import cn.nukkit.plugin.PluginBase
import me.hbj233.wguild.module.WGuildModule
import top.wetabq.easyapi.module.EasyAPIModuleManager
import top.wetabq.easyapi.utils.color

class WGuildPlugin : PluginBase() {


    override fun onEnable() {
        instance = this
        EasyAPIModuleManager.register(WGuildModule)
    }

    companion object{
        var title = "&l&e[&bW&cGuild&e]&r ".color()
        lateinit var instance : WGuildPlugin
    }

}