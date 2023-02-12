package net.galiev.sws

import com.mojang.logging.LogUtils
import dev.syoritohatsuki.duckyupdater.DuckyUpdater
import net.fabricmc.api.ModInitializer
import net.galiev.sws.config.ConfigManager
import net.galiev.sws.event.PlayerFirstJoinCallback
import net.galiev.sws.helper.WorldHelper.getRandInt
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
                    server.worlds.find { it.registryKey.value == Identifier(value[0], value[1]) }
                } ?: return server.close()

                safeCheck(world, blockPos)

                tpSafeZone(player, world, blockPos)
            }
        })

        DuckyUpdater.checkForUpdate("BnoSde42", MOD_ID)
    }
}