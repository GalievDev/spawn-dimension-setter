package dev.galiev.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val dimension: String = "minecraft:overworld",
    val rangeX: Int = 10000,
    val rangeZ: Int = 10000
)