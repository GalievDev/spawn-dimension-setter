package net.galiev.sws.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val dimension: String = "minecraft:overworld",
    val safeCheck: Boolean = true,
    val rangeX: Int = 10000,
    val rangeZ: Int = 10000
)