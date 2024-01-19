package dev.galiev.sds;

import com.mojang.logging.LogUtils;
import dev.galiev.sds.config.ConfigManager;
import dev.galiev.sds.helper.WorldHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SpawnDimensionSetter.MOD_ID)
public class SpawnDimensionSetter {
    public static final String MOD_ID = "sds";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpawnDimensionSetter() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ConfigManager.init();

/*        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
            server.getWorlds().forEach(world -> putWorld(world.getRegistryKey().getValue()));
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            WorldsCommands.register(dispatcher);
        });*/
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        event.getServer().getAllLevels().forEach(WorldHelper::putWorld);
    }
}
