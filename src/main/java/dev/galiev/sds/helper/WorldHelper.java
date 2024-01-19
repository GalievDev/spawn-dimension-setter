package dev.galiev.sds.helper;

import dev.galiev.sds.config.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

import java.util.ArrayList;
import java.util.Random;

public class WorldHelper {
    public static final ArrayList<ServerLevel> dims = new ArrayList<>();
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static void putWorld(ServerLevel world) {
        dims.add(world);
    }

    public static void tpSafeZone(ServerPlayer player, ServerLevel serverWorld, BlockPos.MutableBlockPos blockPos) {
        if (isSafe(serverWorld, blockPos)) {
            player.setRespawnPosition(serverWorld.dimension(), blockPos, player.getRespawnAngle(), true, false);
            player.teleportTo(serverWorld, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, player.yya, player.getVoicePitch());
        } else {
            blockPos.setX(getRandInt(ConfigManager.read().rangeX()));
            blockPos.setZ(getRandInt(ConfigManager.read().rangeZ()));
            safeCheck(serverWorld, blockPos);
            tpSafeZone(player, serverWorld, blockPos);
        }
    }

    public static void safeCheck(ServerLevel serverWorld, BlockPos.MutableBlockPos blockPos) {
        int y = blockPos.getY();
        while (!isSafe(serverWorld, blockPos)) {
            y++;
            blockPos.setY(y);
            if (blockPos.getY() >= 120 && serverWorld.dimension() == Level.NETHER) {
                blockPos.setY(70);
                blockPos.setX(getRandInt(ConfigManager.read().rangeX()));
                blockPos.setZ(getRandInt(ConfigManager.read().rangeZ()));
                safeCheck(serverWorld, blockPos);
            } else if (blockPos.getY() >= 200) {
                blockPos.setY(70);
                blockPos.setX(getRandInt(ConfigManager.read().rangeX()));
                blockPos.setZ(getRandInt(ConfigManager.read().rangeZ()));
                safeCheck(serverWorld, blockPos);
            }
        }
    }


    public static int getRandInt(int bound) {
        return RANDOM.nextInt(bound);
    }

    public static boolean isSafe(ServerLevel world, BlockPos mutableBlockPos) {
        return isEmpty(world, mutableBlockPos) && !isDangerBlocks(world, mutableBlockPos);
    }

    private static boolean isEmpty(ServerLevel world, BlockPos mutableBlockPos) {
        return world.isEmptyBlock(mutableBlockPos.above()) && world.isEmptyBlock(mutableBlockPos);
    }

    private static boolean isDangerBlocks(ServerLevel world, BlockPos mutableBlockPos) {
        if (isDangerBlock(world, mutableBlockPos) && isDangerBlock(world, mutableBlockPos.above()) && isDangerBlock(world, mutableBlockPos.below())) {
            return true;
        } else {
            return world.getBlockState(mutableBlockPos.below()).getBlock() == Blocks.AIR;
        }
    }

    private static boolean isDangerBlock(ServerLevel world, BlockPos mutableBlockPos) {
        return world.getBlockState(mutableBlockPos).getBlock() instanceof LiquidBlock;
    }
}
