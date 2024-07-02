package net.galiev.sws

import com.mojang.logging.LogUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
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
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.slf4j.Logger

object SpawnWorldSetter : ModInitializer {
    const val MOD_ID = "sws"
    val LOGGER: Logger = LogUtils.getLogger();
    override fun onInitialize() {
        ConfigManager

        val x: Int = getRandInt(ConfigManager.read().rangeX)
        val y: Int = 70
        val z: Int = getRandInt(ConfigManager.read().rangeX)

        val blockPos = BlockPos.Mutable(x, y, z)

        PlayerFirstJoinCallback.EVENT.register(object : PlayerFirstJoinCallback.FirstJoin {
            override fun joinServerForFirstTime(player: ServerPlayerEntity, server: MinecraftServer) {

                val world: ServerWorld = ConfigManager.read().dimension.split(":").let { value ->
                    server.worlds.find { it.registryKey.value == Identifier.of(value[0], value[1]) }
                } ?: return server.close()

                if (ConfigManager.read().safeCheck) {
                    safeCheck(world, blockPos)
                    tpSafeZone(player, world, blockPos)
                }
            }
        })

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            server?.worlds?.forEach { world -> putWorld(world.registryKey.value)}
        })

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->  WorldsCommands.register(dispatcher)})
    }
}