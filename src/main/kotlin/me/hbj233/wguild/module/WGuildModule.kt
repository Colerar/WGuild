package me.hbj233.wguild.module

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import me.hbj233.wguild.gui.WGuildMainGUI
import top.wetabq.easyapi.config.encoder.advance.SimpleCodecEasyConfig
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.*
import me.hbj233.wguild.utils.getTodayDate
import top.wetabq.easyapi.api.defaults.*
import top.wetabq.easyapi.command.EasySubCommand
import top.wetabq.easyapi.command.EasyCommand
import top.wetabq.easyapi.config.defaults.SimpleConfigEntry
import top.wetabq.easyapi.module.ModuleInfo
import top.wetabq.easyapi.module.ModuleVersion
import top.wetabq.easyapi.module.SimpleEasyAPIModule

object WGuildModule : SimpleEasyAPIModule() {

    private val plugin = WGuildPlugin.instance
    private var title = WGuildPlugin.title

    //MODULES
    private const val MODULE_NAME = "WGuild"
    private const val AUTHOR = "HBJ233"
    const val SIMPLE_CONFIG = "wguildSimpleConfig"
    const val WGUILD_COMMAND = "wguildCommand"
    const val WGUILD_CONFIG_NAME = "wguildConfig"
    const val WGUILD_SETTINGS_CONFIG_NAME = "wguildSettingsConfig"
    const val WGUILD_POSITIONS_CONFIG_NAME = "wguildPositionsConfig"
    const val WGUILD_POSITIONS_GROUPS_CONFIG_NAME = "wguildPositionsGroupsConfig"
    const val WGUILD_LEVELS_CONFIG_NAME = "wguildLevelsConfig"
    const val WGUILD_LEVELS_GROUPS_CONFIG_NAME = "wguildLevelsGroupsConfig"
    lateinit var wguildConfig: SimpleCodecEasyConfig<WGuildData>
    lateinit var wguildSettingsConfig: SimpleCodecEasyConfig<WGuildSettingData>
    lateinit var wguildPositionsConfig: SimpleCodecEasyConfig<WGuildPositionData>
    lateinit var wguildPositionsGroupsConfig: SimpleCodecEasyConfig<WGuildPositionsGroupData>
    lateinit var wguildLevelsConfig: SimpleCodecEasyConfig<WGuildLevelData>
    lateinit var wguildLevelsGroupConfig: SimpleCodecEasyConfig<WGuildLevelsGroupData>

    //CONFIG PATH
    private const val TITLE_PATH = ".title"

    //PLACEHOLDER
    private const val TITLE_PLACE_HOLDER = "%wguild_title%"


    override fun getModuleInfo(): ModuleInfo = ModuleInfo(
            WGuildPlugin.instance,
            MODULE_NAME,
            AUTHOR,
            ModuleVersion(1, 0, 0)
    )

    override fun moduleRegister() {

        val simpleConfig = this.registerAPI(SIMPLE_CONFIG, SimpleConfigAPI(plugin))
                .add(SimpleConfigEntry(TITLE_PATH, title))

        title = simpleConfig.getPathValue(TITLE_PATH) as String? ?: title

        MessageFormatAPI.registerSimpleFormatter(object : SimpleMessageFormatter {
            override fun format(message: String): String = message.replace(TITLE_PLACE_HOLDER, title)
        })

        wguildPositionsConfig = object : SimpleCodecEasyConfig<WGuildPositionData>(WGUILD_POSITIONS_CONFIG_NAME, plugin, WGuildPositionData::class.java,
                WGuildPositionData("&7&l示例权限", isOwner = false, canKick = false,
                        canInvite = true, canPermitInvite = false, canChangeSetting = false,
                        canDissolve = false, canManageMoney = false)) {}

        if (wguildPositionsConfig.simpleConfig.isNullOrEmpty()) {
            wguildPositionsConfig.simpleConfig = linkedMapOf(
                    "公会长" to WGuildPositionData("&a&l公会长", isOwner = true, canKick = true,
                            canInvite = true, canPermitInvite = true, canChangeSetting = true,
                            canDissolve = true, canManageMoney = true),
                    "长老" to WGuildPositionData("&e&l长老", isOwner = false, canKick = true,
                            canInvite = true, canPermitInvite = true, canChangeSetting = true,
                            canDissolve = false, canManageMoney = true),
                    "成员" to WGuildPositionData("&7&l成员", isOwner = false, canKick = false,
                            canInvite = true, canPermitInvite = false, canChangeSetting = false,
                            canDissolve = false, canManageMoney = false)
            )
        }
        wguildPositionsConfig.save()

        wguildPositionsGroupsConfig = object : SimpleCodecEasyConfig<WGuildPositionsGroupData>(WGUILD_POSITIONS_GROUPS_CONFIG_NAME, plugin, WGuildPositionsGroupData::class.java,
                defaultValue = WGuildPositionsGroupData(arrayListOf("公会长", "长老", "成员"))
        ) {}

        if (wguildPositionsGroupsConfig.simpleConfig.isNullOrEmpty()) {
            wguildPositionsGroupsConfig.simpleConfig["默认权限组"] = wguildPositionsGroupsConfig.getDefaultValue()
            wguildPositionsGroupsConfig.save()
        }

        wguildLevelsConfig = object : SimpleCodecEasyConfig<WGuildLevelData>(WGUILD_LEVELS_CONFIG_NAME, plugin, WGuildLevelData::class.java,
                defaultValue = WGuildLevelData(5, canChangeName = false, canUseColor = false, price = 250)
        ) {}

        if (wguildLevelsConfig.simpleConfig.isNullOrEmpty()) {
            wguildLevelsConfig.simpleConfig = linkedMapOf(
                    "默认等级1" to WGuildLevelData(5, canChangeName = false, canUseColor = false, price = 250),
                    "默认等级2" to WGuildLevelData(10, canChangeName = false, canUseColor = false, price = 500),
                    "默认等级3" to WGuildLevelData(15, canChangeName = true, canUseColor = false, price = 1000),
                    "默认等级4" to WGuildLevelData(20, canChangeName = true, canUseColor = true, price = 2000),
                    "默认等级5" to WGuildLevelData(25, canChangeName = true, canUseColor = true, price = 4000)
            )
            wguildLevelsConfig.save()
        }

        wguildLevelsGroupConfig = object : SimpleCodecEasyConfig<WGuildLevelsGroupData>(WGUILD_LEVELS_GROUPS_CONFIG_NAME, plugin, WGuildLevelsGroupData::class.java,
                defaultValue = WGuildLevelsGroupData(linkedMapOf(
                        "0" to "默认等级1", "1" to "默认等级2", "2" to "默认等级3", "3" to "默认等级4", "4" to "默认等级5")
                )
        ) {}

        if (wguildLevelsGroupConfig.simpleConfig.isNullOrEmpty()) {
            wguildLevelsGroupConfig.simpleConfig["默认等级组"] = wguildLevelsGroupConfig.getDefaultValue()
            wguildLevelsGroupConfig.save()
        }

        wguildSettingsConfig = object : SimpleCodecEasyConfig<WGuildSettingData>(WGUILD_SETTINGS_CONFIG_NAME, plugin, WGuildSettingData::class.java,
                defaultValue = WGuildSettingData(
                        true,
                        guildPositions = "默认权限组",
                        guildLevelsSetting = "默认等级组"
                )
        ) {}

        if (!wguildSettingsConfig.simpleConfig.containsKey("默认") || wguildSettingsConfig.simpleConfig.isEmpty()) {
            wguildSettingsConfig.simpleConfig["默认"] = wguildSettingsConfig.getDefaultValue()
            wguildSettingsConfig.save()
        }

        var defaultUsingSettingsKey = "默认"
        wguildSettingsConfig.simpleConfig.forEach {
            if (it.value.isDefaultSetting) {
                defaultUsingSettingsKey = it.key
            }
        }

        wguildConfig = object : SimpleCodecEasyConfig<WGuildData>(WGUILD_CONFIG_NAME, plugin, WGuildData::class.java,
                defaultValue = WGuildData(
                        guildDisplayName = "ExampleGuild",
                        guildLevel = 0,
                        guildDescription = "这是一个公会示例.",
                        guildMaxMember = 5,
                        guildNowMember = 0,
                        guildMoney = 0,
                        createDate = getTodayDate(),
                        guildActivity = 0.0,
                        isVisible = true,
                        canChangeUsingSettings = false,
                        guildPlayersData = linkedMapOf(),
                        guildAskJoinPlayersName = arrayListOf(),
                        guildInvitedPlayers = linkedMapOf(),
                        usingSettings = defaultUsingSettingsKey
                )) {}
        wguildConfig.init()

        this.registerAPI(WGUILD_CONFIG_NAME, ConfigAPI()).add(wguildConfig)
        this.registerAPI(WGUILD_SETTINGS_CONFIG_NAME, ConfigAPI()).add(wguildSettingsConfig)
        this.registerAPI(WGUILD_LEVELS_CONFIG_NAME, ConfigAPI()).add(wguildLevelsConfig)
        this.registerAPI(WGUILD_LEVELS_GROUPS_CONFIG_NAME, ConfigAPI()).add(wguildLevelsGroupConfig)
        this.registerAPI(WGUILD_POSITIONS_CONFIG_NAME, ConfigAPI()).add(wguildPositionsConfig)
        this.registerAPI(WGUILD_POSITIONS_GROUPS_CONFIG_NAME, ConfigAPI()).add(wguildPositionsGroupsConfig)

        this.registerAPI(WGUILD_COMMAND, CommandAPI())
                .add(object : EasyCommand("wguild") {

                    init {
                        this.aliases = arrayOf("wgd")
                        subCommand.add(object : EasySubCommand("gui") {
                            override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
                                if (sender is Player) {
                                    sender.showFormWindow(WGuildMainGUI(sender))
                                }
                                return true
                            }

                            override fun getAliases(): Array<String>? = arrayOf("g", "ui")

                            override fun getDescription(): String = "Open GUI."

                            override fun getParameters(): Array<CommandParameter>? = null
                        })

                        loadCommandBase()

                    }
                })

    }

    override fun moduleDisable() {
    }


}