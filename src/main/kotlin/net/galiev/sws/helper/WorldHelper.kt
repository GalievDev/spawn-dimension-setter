package net.galiev.sws.helper

import net.galiev.sws.SpawnWorldSetter.LOGGER
import net.galiev.sws.config.ConfigManager
import net.minecraft.block.BedBlock
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.block.RespawnAnchorBlock
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*


object WorldHelper {
    val dims = ArrayList<Identifier>()

    fun putWorld(world: Identifier) {
        dims.add(world)
    }

    fun clearWorlds() {
        dims.clear()
    }

    fun hasRespawnBlocks(player: ServerPlayerEntity) = player.spawnPointDimension?.let { dimension ->
        player.spawnPointPosition?.let { pos ->
            player.server.getWorld(dimension)?.getBlockState(pos)?.block?.let {
                it is RespawnAnchorBlock || it is BedBlock
            }
        }
    } ?: false

    fun tpSafeZone(player: ServerPlayerEntity, serverWorld: ServerWorld, blockPos: BlockPos.Mutable) {
        if (isSafe(serverWorld, blockPos)){
            player.setSpawnPoint(serverWorld.registryKey, blockPos, player.bodyYaw, true, false)
            player.teleport(serverWorld, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), setOf(), player.bodyYaw, player.pitch)
            LOGGER.info("Players spawns: ${serverWorld.registryKey.value} at ${blockPos.x} ${blockPos.y} ${blockPos.y}")
        } else {
            if (ConfigManager.read().isRangeSpawn) {
                blockPos.x = getRandInt(ConfigManager.read().rangeSpawn.rangeX)
                blockPos.z = getRandInt(ConfigManager.read().rangeSpawn.rangeZ)
            }
            safeCheck(serverWorld, blockPos)
            tpSafeZone(player, serverWorld, blockPos)
        }
    }

    fun safeCheck(serverWorld: ServerWorld?, blockPos: BlockPos.Mutable) {
        var y: Int = blockPos.y
        while (!isSafe(serverWorld!!, blockPos)) {
            y++
            blockPos.y = y
            if (blockPos.y >= 120 && serverWorld.registryKey == World.NETHER) {
                blockPos.y = 70
                if (ConfigManager.read().isRangeSpawn) {
                    blockPos.x = getRandInt(ConfigManager.read().rangeSpawn.rangeX)
                    blockPos.z = getRandInt(ConfigManager.read().rangeSpawn.rangeZ)
                    safeCheck(serverWorld, blockPos)
                }
            } else if (blockPos.y >= 200){
                blockPos.y = 70
                if (ConfigManager.read().isRangeSpawn) {
                    blockPos.x = getRandInt(ConfigManager.read().rangeSpawn.rangeX)
                    blockPos.z = getRandInt(ConfigManager.read().rangeSpawn.rangeZ)
                    safeCheck(serverWorld, blockPos)
                }
            }
        }
    }
    fun getRandInt(bound: Int): Int {
        val random = Random()
        return random.nextInt(bound)
    }

    private fun isSafe(world: ServerWorld, mutableBlockPos: BlockPos): Boolean {
        return isEmpty(world, mutableBlockPos) && !isDangerBlocks(world, mutableBlockPos)
    }

    private fun isEmpty(world: ServerWorld, mutableBlockPos: BlockPos): Boolean {
        return world.isAir(mutableBlockPos.add(0, 1, 0)) && world.isAir(mutableBlockPos)
    }

    private fun isDangerBlocks(world: ServerWorld, mutableBlockPos: BlockPos): Boolean {
        return if (isDangerBlock(world, mutableBlockPos) && isDangerBlock(world, mutableBlockPos.add(0, 1, 0)) && isDangerBlock(world, mutableBlockPos.add(0, -1, 0))) {
            true
        } else world.getBlockState(mutableBlockPos.add(0, -1, 0)).block === Blocks.AIR
    }

    private fun isDangerBlock(world: ServerWorld, mutableBlockPos: BlockPos?): Boolean {
        return world.getBlockState(mutableBlockPos).block is FluidBlock
    }
}