package dev.galiev.sds;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SpawnDimensionSetter.MOD_ID)
public class SpawnDimensionSetter {
    public static final String MOD_ID = "sds";

    public SpawnDimensionSetter() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
