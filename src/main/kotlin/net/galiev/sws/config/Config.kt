package net.galiev.sws.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val dimension: String = "minecraft:overworld",
    val safeCheck: Boolean = false,
    val isRangeSpawn: Boolean = true,
    val isExactSpawn: Boolean = false,
    val rangeSpawn: RangeSpawn = RangeSpawn(),
    val exactSpawn: ExactSpawn = ExactSpawn(0, 0, 0)
)