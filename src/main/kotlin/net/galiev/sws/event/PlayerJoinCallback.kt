package net.galiev.sws.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

object PlayerJoinCallback {
    val JOIN: Event<JoinEvent> = EventFactory.createArrayBacked(
        JoinEvent::class.java
    )
    { callbacks: Array<JoinEvent> ->
        object : JoinEvent {
            override fun joinServer(player: ServerPlayerEntity, server: MinecraftServer) {
                callbacks.forEach {
                    it.joinServer(player, server)
                }
            }
        }
    }

    interface JoinEvent {
        fun joinServer(player: ServerPlayerEntity, server: MinecraftServer)
    }
}