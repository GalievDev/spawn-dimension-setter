package net.galiev.sws.config

import kotlinx.serialization.Serializable

@Serializable
data class ExactSpawn(
    val x: Int,
    val y: Int,
    val z: Int
)