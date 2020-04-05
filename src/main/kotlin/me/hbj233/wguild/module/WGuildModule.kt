package me.hbj233.wguild.module

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerChatEvent
import cn.nukkit.event.player.PlayerRespawnEvent
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
import top.wetabq.easyapi.module.defaults.ChatNameTagFormatModule
import top.wetabq.easyapi.utils.color

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
    const val WGUILD_LISTENER = "wguildListener"
    const val PLACEHOLDER_PLAYER_GUILD = "%wguild_player%"
    lateinit var wguildConfig: SimpleCodecEasyConfig<WGuildData>
    lateinit var wguildPlayerConfig: SimpleCodecEasyConfig<PlayerGuildData>
    lateinit var wguildSettingsConfig: SimpleCodecEasyConfig<WGuildSettingData>
    lateinit var wguildPositionsGroupsConfig: SimpleCodecEasyConfig<WGuildPositionsGroupData>
    lateinit var defaultPositionPair: Triple<String, WGuildPositionsGroupData, WGuildPositionData>
    lateinit var defaultSettingPair: Triple<String, WGuildSettingData, WGuildLevelData>

    //CONFIG PATH
    private const val PATH_TITLE = "title"
    private const val PATH_GUILD_CHAT_START_WITH = "guildChatStarWith"

    //PLACEHOLDER
    const val PLACEHOLDER_TITLE = "%wguild_title%"
    const val PLACEHOLDER_PLAYER_IS_GUILD_CHAT_MODE = "%wguild_player_guild_chat_mode%"
    const val PLACEHOLDER_PLAYER_HAS_INVITATION = "%wguild_player_has_invitation%"
    const val PLACEHOLDER_PLAYER_GUILD_POSITION = "%wguild_player_guild_position%"


    override fun getModuleInfo(): ModuleInfo = ModuleInfo(
            WGuildPlugin.instance,
            MODULE_NAME,
            AUTHOR,
            ModuleVersion(1, 0, 0)
    )

    override fun moduleRegister() {

        val simpleConfig = this.registerAPI(SIMPLE_CONFIG, SimpleConfigAPI(plugin))
                .add(SimpleConfigEntry(PATH_TITLE, title))
                .add(SimpleConfigEntry(PATH_GUILD_CHAT_START_WITH, "!"))

        title = simpleConfig.getPathValue(PATH_TITLE).toString()
        val guildChatStartWith = simpleConfig.getPathValue(PATH_GUILD_CHAT_START_WITH).toString()

        val easyAPIChatConfig = ChatNameTagFormatModule.getIntegrateAPI(ChatNameTagFormatModule.CHAT_CONFIG) as SimpleConfigAPI
        val nameTagConfigValue = easyAPIChatConfig.getPathValue(ChatNameTagFormatModule.NAME_TAG_FORMAT_PATH)
        if (!nameTagConfigValue.toString().contains(PLACEHOLDER_PLAYER_GUILD)) {
            easyAPIChatConfig.setPathValue(SimpleConfigEntry(ChatNameTagFormatModule.NAME_TAG_FORMAT_PATH, "$PLACEHOLDER_PLAYER_GUILD&r$nameTagConfigValue"))
        }


        MessageFormatAPI.registerSimpleFormatter(object : SimpleMessageFormatter {
            override fun format(message: String): String = message.replace(PLACEHOLDER_TITLE, title)
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
        wguildPositionsGroupsConfig.init()

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
        wguildSettingsConfig.init()

        if (!wguildSettingsConfig.simpleConfig.containsKey("默认权限组") || wguildPositionsGroupsConfig.simpleConfig.isNullOrEmpty()) {
            wguildPositionsGroupsConfig.simpleConfig["默认权限组"] = wguildPositionsGroupsConfig.getDefaultValue()
            wguildPositionsGroupsConfig.save()
        }

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
                        signInAward = 100,
                        createDate = getTodayDate(),
                        guildActivity = 0.0,
                        isVisible = false,
                        guildPlayersData = linkedMapOf(),
                        guildAskJoinPlayersName = arrayListOf(),
                        guildInvitedPlayers = linkedMapOf(),
                        usingSettings = defaultUsingSettingsKey,
                        canPVP = false,
                        usingPositionSettings = defaultUsingPositionKey
                )) {}
        wguildConfig.init()

        wguildPlayerConfig = object : SimpleCodecEasyConfig<PlayerGuildData>(WGUILD_PLAYER_CONFIG, plugin, PlayerGuildData::class.java,
                defaultValue = PlayerGuildData("", false, linkedMapOf())) {}
        wguildPlayerConfig.init()

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

        this.registerAPI(WGUILD_LISTENER, NukkitListenerAPI(plugin))
                .add(object : Listener {

                    @EventHandler
                    fun onPlayerAttack(event: EntityDamageEvent) {
                        if (event is EntityDamageByEntityEvent) {
                            val damager = event.damager
                            val entity = event.entity
                            if (damager is Player && entity is Player) {
                                val damagerGuildId = wguildPlayerConfig.safeGetData(damager.name).playerJoinGuildId
                                val entityGuildId = wguildPlayerConfig.safeGetData(entity.name).playerJoinGuildId
                                if (damagerGuildId != "" && entityGuildId != "" && damagerGuildId == entityGuildId) {
                                    if (!wguildConfig.safeGetData(damagerGuildId).canPVP) {
                                        event.setCancelled()
                                    }
                                }
                            }
                        }
                    }

                    @EventHandler
                    fun onPlayerChat(event: PlayerChatEvent) {
                        val player = event.player
                        val playerData = wguildPlayerConfig.safeGetData(player.name)
                        if (playerData.playerJoinGuildId != "" && (playerData.isGuildChatMode || event.message.startsWith(guildChatStartWith))) {
                            val targetGuildData = wguildConfig.safeGetData(playerData.playerJoinGuildId)
                            targetGuildData.sendGuildMessage(player.nameTag, player.name, event.message, guildChatStartWith)
                            event.setCancelled()
                        }
                    }

                    @EventHandler(priority = EventPriority.LOWEST)
                    fun onPlayerRespawn(event: PlayerRespawnEvent) {
                        SimplePluginTaskAPI.delay(10) { _, _ ->
                            event.player.teleport(event.player.level.spawnLocation.add(0.0, 1.0, 0.0))
                        }
                    }


                })

        val messageFormatter = { msg: String, player: String ->
            var final = msg
            val playerData = wguildPlayerConfig.safeGetData(player)
            if (playerData.playerJoinGuildId != "") {
                val targetGuildData = wguildConfig.safeGetData(playerData.playerJoinGuildId)
                final = final
                        .replace(PLACEHOLDER_PLAYER_GUILD, "&a[&b${targetGuildData.guildDisplayName}&a]")
                        .replace(PLACEHOLDER_PLAYER_GUILD_POSITION, "&6[&a${targetGuildData.getPlayerPosition(player).displayName}&6]")
                        .replace(PLACEHOLDER_PLAYER_IS_GUILD_CHAT_MODE, if (playerData.isGuildChatMode) "&a已启用" else "&c已禁用")
                        .replace(PLACEHOLDER_PLAYER_HAS_INVITATION, if(playerData.receivedInvite.isEmpty()) "&a有新的邀请" else "&7暂时没有邀请")
                        .color()
            } else {
                final = final
                        .replace(PLACEHOLDER_PLAYER_GUILD, "")
                        .replace(PLACEHOLDER_PLAYER_GUILD_POSITION, "")
                        .replace(PLACEHOLDER_PLAYER_IS_GUILD_CHAT_MODE, "")
                        .replace(PLACEHOLDER_PLAYER_HAS_INVITATION, "")
            }
            final
        }

        MessageFormatAPI.registerSimplePlayerFormatter { msg, player ->
            messageFormatter(msg, player.name)
        }

        MessageFormatAPI.registerFormatter("wguildChatFormatter", PlayerChatEvent::class.java, object : MessageFormatter<PlayerChatEvent> {
            override fun format(message: String, data: PlayerChatEvent): String = messageFormatter(message, data.player.name)
        })

        MessageFormatAPI.registerFormatter("wguildNameTagFormatter", String::class.java, object : MessageFormatter<String> {
            override fun format(message: String, data: String): String {
                if (getModuleInfo().moduleOwner.server.getPlayer(data) is Player) {
                    return messageFormatter(message, data)
                }
                return message
            }
        })

    }

    override fun moduleDisable() {
    }


}