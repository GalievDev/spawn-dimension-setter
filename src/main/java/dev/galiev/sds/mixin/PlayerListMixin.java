package dev.galiev.sds.mixin;

import dev.galiev.sds.config.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.galiev.sds.helper.WorldHelper.*;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(at = @At(value = "TAIL"), method = "placeNewPlayer")
    private void playerFirstJoin(Connection pNetManager, ServerPlayer pPlayer, CallbackInfo ci) {
        if (pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) < 1) {
            int x = getRandInt(ConfigManager.read().rangeX());
            int y = 70;
            int z = getRandInt(ConfigManager.read().rangeZ());
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(x, y, z);

            ServerLevel world = null;
            String[] value = ConfigManager.read().dimension().split(":");
            for (ServerLevel serverWorld : dims) {
                if (serverWorld.dimension().registry().equals(new ResourceLocation(value[0], value[1]))) {
                    world = serverWorld;
                    safeCheck(world, blockPos);
                    tpSafeZone(pPlayer, world, blockPos);
                    break;
                }
            }
            if (world == null) {
                pPlayer.getServer().close();
            }
        }
    }
}
