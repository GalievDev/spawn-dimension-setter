package dev.galiev.sds.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.galiev.sds.SpawnDimensionSetter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static dev.galiev.sds.SpawnDimensionSetter.MOD_ID;

public class ConfigManager {
    private static final File configDir = Paths.get("", "config", MOD_ID).toFile();
    private static final File configFile = new File(configDir, "config.json");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Config defaultConfig = new Config(1000, 1000, "minecraft:overworld");

    public static void init() {
        if (!configDir.exists() && !configDir.mkdirs()) {
            SpawnDimensionSetter.LOGGER.warn("Can't create config dirs");
        }

        if (!configFile.exists()) {
            try {
                if (!configFile.createNewFile()) {
                    SpawnDimensionSetter.LOGGER.warn("Can't create config file");
                    return;
                }
                Files.writeString(configFile.toPath(), gson.toJson(defaultConfig));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Config read() {
        try {
            return gson.fromJson(Files.readString(configFile.toPath()), Config.class);
        } catch (Exception e) {
            SpawnDimensionSetter.LOGGER.error("Can't read config or it don't exist");
            try {
                SpawnDimensionSetter.LOGGER.info("Backup config...");
                Files.copy(configFile.toPath(), new File(configDir, "backup_config.json").toPath());
                Files.writeString(configFile.toPath(), gson.toJson(defaultConfig));
                return gson.fromJson(Files.readString(configFile.toPath()), Config.class);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
