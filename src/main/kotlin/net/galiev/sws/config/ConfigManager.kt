package net.galiev.sws.config

import kotlinx.serialization.json.Json
import net.galiev.sws.helper.WorldHelper.getRandInt
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.io.File
import java.nio.file.Paths

object ConfigManager {
    private val json = Json { encodeDefaults = true; prettyPrint = true; ignoreUnknownKeys = true }
    private val configDir: File = Paths.get("", "config", "spawn_dimension_setter").toFile()
    private val configFile = File(configDir, "config.json")

    init {
        if (!configFile.exists()) {
            if (!configDir.exists()) configDir.mkdirs()
            configFile.apply {
                createNewFile()
                writeText(json.encodeToString(Config()))
            }
        } else configFile.writeText(json.encodeToString(read()))
    }

    fun read(): Config {
        return json.decodeFromString(configFile.readText())
    }

    fun write(config: Config) {
        if (!configDir.exists()) configDir.mkdirs()
        configFile.writeText(json.encodeToString(config))
    }

    fun getDimensionWorld(server: MinecraftServer): ServerWorld? {
        val (namespace, path) = read().dimension.split(":")
        val id = Identifier.of(namespace, path)
        return server.worlds.find { it.registryKey.value == id }
    }

    fun spawnBlockPos(): BlockPos =
        BlockPos(read().exactSpawn.x, read().exactSpawn.y, read().exactSpawn.z)

    fun blockPos(): BlockPos.Mutable {
        var x = 0
        var y = 70
        var z = 0

        if (read().isRangeSpawn) {
            x = getRandInt(read().rangeSpawn.rangeX)
            z = getRandInt(read().rangeSpawn.rangeZ)
        } else if (read().isExactSpawn) {
            x = read().exactSpawn.x
            y = read().exactSpawn.y
            z = read().exactSpawn.z
        }

        return BlockPos.Mutable(x, y, z)
    }
}