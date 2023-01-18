package net.galiev.sws.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val dimension: String = "minecraft:overworld"
)