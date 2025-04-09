package net.galiev.sws

import com.mojang.logging.LogUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.galiev.sws.commands.WorldsCommands
import net.galiev.sws.config.ConfigManager
import net.galiev.sws.event.PlayerFirstJoinCallback
import net.galiev.sws.helper.WorldHelper.getRandInt
import net.galiev.sws.helper.WorldHelper.putWorld
import net.galiev.sws.helper.WorldHelper.safeCheck
import net.galiev.sws.helper.WorldHelper.tpSafeZone
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.network.ServerPlayerEntity.Respawn
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger

object SpawnWorldSetter : ModInitializer {
    const val MOD_ID = "sws"
    val LOGGER: Logger = LogUtils.getLogger();
    override fun onInitialize() {
        ConfigManager

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            server?.worlds?.forEach { world -> putWorld(world.registryKey.value) }
        })

        var x = 0
        var y = 70
        var z = 0

        if (ConfigManager.read().isRangeSpawn) {
            x = getRandInt(ConfigManager.read().rangeSpawn.rangeX)
            z = getRandInt(ConfigManager.read().rangeSpawn.rangeZ)
        } else if (ConfigManager.read().isExactSpawn) {
            x = ConfigManager.read().exactSpawn.x
            y = ConfigManager.read().exactSpawn.y
            z = ConfigManager.read().exactSpawn.z
        }

        val blockPos = BlockPos.Mutable(x, y, z)

        PlayerFirstJoinCallback.EVENT.register(object : PlayerFirstJoinCallback.FirstJoin {
            override fun joinServerForFirstTime(player: ServerPlayerEntity, server: MinecraftServer) {

                val world: ServerWorld = ConfigManager.read().dimension.split(":").let { value ->
                    server.worlds.find { it.registryKey.value == Identifier.of(value[0], value[1]) }
                } ?: return server.close()

                if (ConfigManager.read().safeCheck && ConfigManager.read().isRangeSpawn) {
                    safeCheck(world, blockPos)
                    tpSafeZone(player, world, blockPos)
                } else {
                    player.setSpawnPoint(Respawn(world.registryKey, blockPos, player.bodyYaw, true), false)
                    player.teleport(world, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), setOf(), player.bodyYaw, player.pitch, false)
                    LOGGER.info("Players spawns: ${world.registryKey.value} at $x $y $z")
                }
            }
        })

        ServerPlayerEvents.AFTER_RESPAWN.register(ServerPlayerEvents.AfterRespawn { oldPlayer, newPlayer, alive ->
            val server = newPlayer.server
            val world: ServerWorld = ConfigManager.read().dimension.split(":").let { value ->
                server.worlds.find { it.registryKey.value == Identifier.of(value[0], value[1]) }
            } ?: return@AfterRespawn

            val respawn = newPlayer.respawn ?: Respawn(world.registryKey, blockPos, newPlayer.bodyYaw, true)
            val oldRespawn = oldPlayer.respawn ?: Respawn(world.registryKey, blockPos, oldPlayer.bodyYaw, true)
            if (respawn != oldRespawn) {
                newPlayer.setSpawnPoint(respawn, false)
                newPlayer.teleport(
                    world,
                    blockPos.x.toDouble(),
                    blockPos.y.toDouble(),
                    blockPos.z.toDouble(),
                    setOf(),
                    newPlayer.bodyYaw,
                    newPlayer.pitch,
                    false
                )
            }
        })

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->  WorldsCommands.register(dispatcher)})
    }
}