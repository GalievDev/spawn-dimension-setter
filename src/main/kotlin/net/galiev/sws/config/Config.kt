package net.galiev.sws.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val dimension: String = "minecraft:overworld",
    val rangeX: Int = 100000,
    val rangeZ: Int = 100000
)