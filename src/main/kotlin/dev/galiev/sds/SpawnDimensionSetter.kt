package dev.galiev.sds

import dev.galiev.config.ConfigManager
import dev.galiev.helper.WorldHelper
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(SpawnDimensionSetter.ID)
object SpawnDimensionSetter {
    const val ID = "sds"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        MinecraftForge.EVENT_BUS.register(this)
        ConfigManager
    }

    @Mod.EventBusSubscriber(modid = ID)
    class ForgeEvents {
        companion object {
            @SubscribeEvent
            fun onPlayerJoin(event: EntityJoinLevelEvent) {
                val server = event.level.server
                LOGGER.info("Server started")
                server?.playerList?.players?.forEach { serverPlayer ->
                    if (serverPlayer.stats.getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) {
                        val level: ServerLevel = ConfigManager.read().dimension.split(":").let { value ->
                            server.allLevels.find { it.dimension().location() == ResourceLocation(value[0], value[1]) }
                        } ?: return server.close()
                        val x = WorldHelper.getRandInt(ConfigManager.read().rangeX)
                        val y = 70
                        val z = WorldHelper.getRandInt(ConfigManager.read().rangeZ)
                        val pos = BlockPos.MutableBlockPos(x, y, z)
                        if (WorldHelper.safeCheck(level, pos)) {
                            WorldHelper.tpSafeZone(serverPlayer, level, pos)
                        } else {
                            LOGGER.warn("Safe position doesn't exists")
                        }
                    } else {
                        LOGGER.warn("Player $serverPlayer already sign up")
                    }
                }
            }
        }
    }
}