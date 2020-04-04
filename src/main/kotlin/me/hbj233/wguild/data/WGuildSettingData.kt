package me.hbj233.wguild.data

import me.hbj233.wguild.module.WGuildModule

/*import me.hbj233.wguild.utils.changeStaticFinal*/

data class WGuildSettingData(
        var isDefaultSetting: Boolean = false,
        val guildLevelsSetting: WGuildLevelsGroupData
) {

    /*fun setToDefaultSetting(key: String) {
        if (isDefaultSetting) {
            /*SimpleCodecEasyConfig::class.java.declaredFields.filter {
                it.name == "defaultValue"
            }.forEach {
                if (it.type == WGuildSettingData::javaClass){
                    changeStaticFinal(it, WGuildModule.wguildSettingsConfig.safeGetData(key))
                }
            }*/
            WGuildModule.wguildSettingsConfig.simpleConfig.filterNot {
                it.key == key
            }.values.forEach {
                it.isDefaultSetting = false
            }
        }
    }*/
}