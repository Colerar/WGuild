package me.hbj233.wguild.utils

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.ExplodeParticle
import cn.nukkit.math.Vector3
import top.wetabq.easyapi.api.defaults.SimplePluginTaskAPI
import java.util.*
import kotlin.math.tanh

fun popItem(player: Player, showPlayerCollection: Collection<Player>) {
    val canPopItemList = arrayOf(
            Item.get(Item.EXPERIENCE_BOTTLE, 0, 2),
            Item.get(Item.FIRE, 0, 2),
            Item.get(Item.GOLD_NUGGET, 0, 2),
            Item.get(Item.CHEST, 0, 2),
            Item.get(Item.DIAMOND, 0, 2),
            Item.get(Item.EMERALD, 0, 2),
            Item.get(Item.DYE, 4, 2),
            Item.get(Item.IRON_INGOT, 0, 2),
            Item.get(Item.GOLDEN_APPLE_ENCHANTED, 0, 2)
    )

    var times = 0
    SimplePluginTaskAPI.repeating(1) { task, _ ->
        if (times >= 60) task.cancel()
        player.level.dropItem(player, canPopItemList[Random().nextInt(canPopItemList.size)], Vector3(
                Math.random() * 0.3 * tanh(Random().nextInt(1000) - 500.0), 0.8,
                Math.random() * 0.3 * tanh(Random().nextInt(1000) - 500.0)), 20*20)
        player.level.addSound(player, Sound.RANDOM_EXPLODE, 1.0F, 1.0F, showPlayerCollection)
        player.level.addSound(player, Sound.RANDOM_ORB, 1.0F, 1.0F, showPlayerCollection)
        player.level.addParticle(ExplodeParticle(player), showPlayerCollection)
        times++
    }
    SimplePluginTaskAPI.delay(20*10) { _, _ ->
        player.level.entities.filter { it.namedTag.getShort("PickupDelay") == 20*20 }.forEach { it.kill() }
    }
}