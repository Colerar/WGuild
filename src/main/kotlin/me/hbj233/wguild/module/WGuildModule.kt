package me.hbj233.wguild.module

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import me.hbj233.wguild.WGuildPlugin
import me.hbj233.wguild.data.*
import me.hbj233.wguild.gui.WGuildMainGUI
import me.hbj233.wguild.utils.getTodayDate
import top.wetabq.easyapi.api.defaults.*
import top.wetabq.easyapi.command.EasyCommand
import top.wetabq.easyapi.command.EasySubCommand
import top.wetabq.easyapi.config.defaults.SimpleConfigEntry
import top.wetabq.easyapi.config.encoder.advance.SimpleCodecEasyConfig
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
    const val WGUILD_PLAYER_CONFIG = "wguildPlayerConfig"
    const val WGUILD_SETTINGS_CONFIG_NAME = "wguildSettingsConfig"
    const val WGUILD_POSITIONS_GROUPS_CONFIG_NAME = "wguildPositionsGroupsConfig"
    lateinit var wguildConfig: SimpleCodecEasyConfig<WGuildData>
    lateinit var wguildPlayerConfig: SimpleCodecEasyConfig<PlayerGuildData>
    lateinit var wguildSettingsConfig: SimpleCodecEasyConfig<WGuildSettingData>
    lateinit var wguildPositionsGroupsConfig: SimpleCodecEasyConfig<WGuildPositionsGroupData>
    lateinit var defaultPositionPair: Triple<String, WGuildPositionsGroupData, WGuildPositionData>
    lateinit var defaultSettingPair: Triple<String, WGuildSettingData, WGuildLevelData>

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

        wguildPositionsGroupsConfig = object : SimpleCodecEasyConfig<WGuildPositionsGroupData>(WGUILD_POSITIONS_GROUPS_CONFIG_NAME, plugin, WGuildPositionsGroupData::class.java,
                defaultValue = WGuildPositionsGroupData(isDefaultSetting = true, ownerGuild = "SYSTEM", positionsGroup = linkedMapOf(
                        "公会长" to WGuildPositionData("&a&l公会长", isOwner = true, canKick = true,
                                canInvite = true, canPermitInvite = true, canChangeSetting = true,
                                canDisband = true, canManageMoney = true, isDefault = false),
                        "长老" to WGuildPositionData("&e&l长老", isOwner = false, canKick = true,
                                canInvite = true, canPermitInvite = true, canChangeSetting = true,
                                canDisband = false, canManageMoney = true, isDefault = false),
                        "成员" to WGuildPositionData("&7&l成员", isOwner = false, canKick = false,
                                canInvite = true, canPermitInvite = false, canChangeSetting = false,
                                canDisband = false, canManageMoney = false, isDefault = true)
                ))
        ) {}

        if (!wguildSettingsConfig.simpleConfig.containsKey("默认权限组") || wguildPositionsGroupsConfig.simpleConfig.isNullOrEmpty()) {
            wguildPositionsGroupsConfig.simpleConfig["默认权限组"] = wguildPositionsGroupsConfig.getDefaultValue()
            wguildPositionsGroupsConfig.save()
        }

        wguildSettingsConfig = object : SimpleCodecEasyConfig<WGuildSettingData>(WGUILD_SETTINGS_CONFIG_NAME, plugin, WGuildSettingData::class.java,
                defaultValue = WGuildSettingData(
                        true,
                        guildLevelsSetting = WGuildLevelsGroupData(linkedMapOf(
                                "0" to WGuildLevelData(5, canChangePositionSetting = false, canChangeName = false, canUseColor = false, canVisible = false, canPVP = false, price = 250),
                                "1" to WGuildLevelData(10, canChangePositionSetting = false,canChangeName = false, canUseColor = false, canVisible = true, canPVP = true, price = 500),
                                "2" to WGuildLevelData(15, canChangePositionSetting = false, canChangeName = true, canUseColor = false, canVisible = true, canPVP = true, price = 1000),
                                "3" to WGuildLevelData(20, canChangePositionSetting = false, canChangeName = true, canUseColor = true, canVisible = true, canPVP = true, price = 2000),
                                "4" to WGuildLevelData(25, canChangePositionSetting = true, canChangeName = true, canUseColor = true, canVisible = true, canPVP = true, price = 4000))
                        )
                )
        ) {}

        if (!wguildSettingsConfig.simpleConfig.containsKey("默认") || wguildSettingsConfig.simpleConfig.isEmpty()) {
            wguildSettingsConfig.simpleConfig["默认"] = wguildSettingsConfig.getDefaultValue()
            wguildSettingsConfig.save()
        }


        var defaultUsingSettingsKey = "默认"
        wguildSettingsConfig.simpleConfig.forEach {
            if (it.value.isDefaultSetting) {
                defaultSettingPair = Triple(it.key, it.value, it.value.guildLevelsSetting.levelsGroup.toSortedMap().values.first())
                defaultUsingSettingsKey = it.key
            }
        }

        var defaultUsingPositionKey = "默认权限组"
        wguildPositionsGroupsConfig.simpleConfig.forEach {
            if (it.value.isDefaultSetting) {
                defaultPositionPair = Triple(
                        it.key,
                        it.value,
                        it.value.positionsGroup
                                .filter { g -> g.value.isDefault }.values
                                .lastOrNull()
                                ?: it.value.positionsGroup.values.first()
                )
                defaultUsingPositionKey = it.key
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
                        guildPlayersData = linkedMapOf(),
                        guildAskJoinPlayersName = arrayListOf(),
                        guildInvitedPlayers = linkedMapOf(),
                        usingSettings = defaultUsingSettingsKey,
                        canPVP = false,
                        usingPositionSettings = defaultUsingPositionKey
                )) {}
        wguildConfig.init()

        wguildPlayerConfig = object : SimpleCodecEasyConfig<PlayerGuildData>(WGUILD_PLAYER_CONFIG, plugin, PlayerGuildData::class.java,
                defaultValue = PlayerGuildData("", linkedMapOf())) {}

        this.registerAPI(WGUILD_CONFIG_NAME, ConfigAPI()).add(wguildConfig)
        this.registerAPI(WGUILD_SETTINGS_CONFIG_NAME, ConfigAPI()).add(wguildSettingsConfig)
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