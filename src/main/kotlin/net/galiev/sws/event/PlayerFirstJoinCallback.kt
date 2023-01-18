package net.galiev.sws.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

object PlayerFirstJoinCallback {
    val EVENT: Event<FirstJoin> = EventFactory.createArrayBacked(
        FirstJoin::class.java
    )
    {
        callbacks: Array<FirstJoin> ->
        object : FirstJoin {
            override fun joinServerForFirstTime(player: ServerPlayerEntity, server: MinecraftServer) {
                callbacks.forEach {
                    it.joinServerForFirstTime(player, server)
                }
            }
        }
    }
    interface FirstJoin {
        fun joinServerForFirstTime(player: ServerPlayerEntity, server: MinecraftServer)
    }
}