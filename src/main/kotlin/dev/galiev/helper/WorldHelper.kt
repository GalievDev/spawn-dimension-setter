package dev.galiev.helper

import dev.galiev.config.ConfigManager
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import java.util.*

object WorldHelper {
    fun tpSafeZone(player: ServerPlayer, serverWorld: ServerLevel, blockPos: BlockPos.MutableBlockPos) {
        if (isSafe(serverWorld, blockPos)){
            player.setRespawnPosition(serverWorld.dimension(), blockPos, player.respawnAngle, true, false)
            player.teleportTo(serverWorld, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), player.yya, player.voicePitch)
        } else {
            blockPos.x = getRandInt(ConfigManager.read().rangeX)
            blockPos.z = getRandInt(ConfigManager.read().rangeX)
            safeCheck(serverWorld, blockPos)
            tpSafeZone(player, serverWorld, blockPos)
        }
    }

    fun safeCheck(serverWorld: ServerLevel, blockPos: BlockPos.MutableBlockPos): Boolean {
        var y: Int = blockPos.y
        while (!isSafe(serverWorld, blockPos)) {
            y++
            blockPos.y = y
            if (blockPos.y >= 120 && serverWorld.dimension().location() == Level.NETHER) {
                blockPos.y = 70
                blockPos.x = getRandInt(ConfigManager.read().rangeX)
                blockPos.z = getRandInt(ConfigManager.read().rangeX)
                safeCheck(serverWorld, blockPos)
            } else if (blockPos.y >= 200){
                blockPos.y = 70
                blockPos.x = getRandInt(ConfigManager.read().rangeX)
                blockPos.z = getRandInt(ConfigManager.read().rangeX)
                safeCheck(serverWorld, blockPos)
            }
            return false
        }
        return true
    }
    fun getRandInt(bound: Int): Int {
        val random = Random()
        return random.nextInt(bound)
    }

    fun isSafe(world: ServerLevel, mutableBlockPos: BlockPos): Boolean {
        return isEmpty(world, mutableBlockPos) && !isDangerBlocks(world, mutableBlockPos)
    }

    private fun isEmpty(world: ServerLevel, mutableBlockPos: BlockPos): Boolean {
        return world.isEmptyBlock(mutableBlockPos.above()) && world.isEmptyBlock(mutableBlockPos)
    }

    private fun isDangerBlocks(world: ServerLevel, mutableBlockPos: BlockPos): Boolean {
        return if (isDangerBlock(world, mutableBlockPos) && isDangerBlock(world, mutableBlockPos.above()) && isDangerBlock(world, mutableBlockPos.below())) {
            true
        } else world.getBlockState(mutableBlockPos.below()).block === Blocks.AIR
    }

    private fun isDangerBlock(world: ServerLevel, mutableBlockPos: BlockPos): Boolean {
        return world.getBlockState(mutableBlockPos).block is LiquidBlock
    }
}