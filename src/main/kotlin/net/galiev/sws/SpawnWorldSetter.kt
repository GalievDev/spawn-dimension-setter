package net.galiev.sws

import com.mojang.logging.LogUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.galiev.sws.commands.WorldsCommands
import net.galiev.sws.config.ConfigManager
import net.galiev.sws.event.PlayerFirstJoinCallback
import net.galiev.sws.helper.WorldHelper.hasRespawnBlocks
import net.galiev.sws.helper.WorldHelper.putWorld
import net.galiev.sws.helper.WorldHelper.safeCheck
import net.galiev.sws.helper.WorldHelper.tpSafeZone
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import org.slf4j.Logger

object SpawnWorldSetter : ModInitializer {
    const val MOD_ID = "sws"
    val LOGGER: Logger = LogUtils.getLogger()

    override fun onInitialize() {
        ConfigManager

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            server?.worlds?.forEach { world -> putWorld(world.registryKey.value) }
        })

        PlayerFirstJoinCallback.EVENT.register(object : PlayerFirstJoinCallback.FirstJoin {
            override fun joinServerForFirstTime(player: ServerPlayerEntity, server: MinecraftServer) {
                val blockPos = ConfigManager.blockPos()
                val world: ServerWorld = ConfigManager.getDimensionWorld(server)?: return server.close()

                if (ConfigManager.read().safeCheck && ConfigManager.read().isRangeSpawn) {
                    safeCheck(world, blockPos)
                    tpSafeZone(player, world, blockPos)
                } else {
                    player.setSpawnPoint(world.registryKey, blockPos, player.yaw, true, false)
                    player.teleport(
                        world,
                        blockPos.x.toDouble(), blockPos.y.toDouble(),
                        blockPos.z.toDouble(),
                        setOf(),
                        player.yaw,
                        player.pitch,
                        false
                    )
                    LOGGER.info("Players spawns: ${world.registryKey.value} at ${blockPos.x} ${blockPos.y} ${blockPos.z}")
                }
            }
        })

        ServerPlayerEvents.AFTER_RESPAWN.register(ServerPlayerEvents.AfterRespawn { _, newPlayer, _ ->
            val server = newPlayer.server

            val world: ServerWorld = ConfigManager.getDimensionWorld(server) ?: return@AfterRespawn server.close()

            val blockPos = ConfigManager.spawnBlockPos()

            if (!hasRespawnBlocks(newPlayer)) {
                newPlayer.setSpawnPoint(world.registryKey, blockPos, newPlayer.yaw, true, false)
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