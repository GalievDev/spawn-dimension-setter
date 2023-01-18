package net.galiev.sws.helper

import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.*

object WorldHelper {
    fun getRandInt(bound: Int): Int {
        val random = Random()
        return random.nextInt(bound)
    }

    fun isSafe(world: ServerWorld, mutableBlockPos: BlockPos): Boolean {
        return isEmpty(world, mutableBlockPos) && !isDangerBlocks(world, mutableBlockPos)
    }

    private fun isEmpty(world: ServerWorld, mutableBlockPos: BlockPos): Boolean {
        return world.isAir(mutableBlockPos.add(0, 1, 0)) && world.isAir(mutableBlockPos)
    }

    private fun isDangerBlocks(world: ServerWorld, mutableBlockPos: BlockPos): Boolean {
        if (isDangerBlock(world, mutableBlockPos) && isDangerBlock(world, mutableBlockPos.add(0, 1, 0)) &&
            isDangerBlock(world, mutableBlockPos.add(0, -1, 0))
        ) {
            return true
        }
        return world.getBlockState(mutableBlockPos.add(0, -1, 0)).block === Blocks.AIR
    }

    private fun isDangerBlock(world: ServerWorld, mutableBlockPos: BlockPos?): Boolean {
        return world.getBlockState(mutableBlockPos).block is FluidBlock && world.getBlockState(mutableBlockPos).block === Blocks.BEDROCK
    }
}