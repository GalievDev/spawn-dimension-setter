package net.galiev.sws.config

import kotlinx.serialization.Serializable

@Serializable
data class RangeSpawn(
    val rangeX: Int = 10000,
    val rangeZ: Int = 10000
)