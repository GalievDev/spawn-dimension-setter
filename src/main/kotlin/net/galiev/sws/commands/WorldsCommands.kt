package net.galiev.sws.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.galiev.sws.helper.WorldHelper.dims
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting

object WorldsCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>){
        dispatcher.register(
            CommandManager.literal("sws")
                .then(CommandManager.literal("worlds").executes { getAllWorlds(it) })
                .then(CommandManager.literal("playerWorld").executes { getPlayerWorld(it) })
        )
    }

    private fun getAllWorlds(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback(LiteralText("All Worlds in your Minecraft: ").styled{it.withColor(Formatting.BLUE).withBold(true)}, false)
        for (dim in dims) {
            context.source.sendFeedback(((LiteralText(" - "))).append(((LiteralText("${dim.namespace}:")).formatted(Formatting.GREEN))
                .append((LiteralText(dim.path)).formatted(Formatting.GRAY))).styled { it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, LiteralText("Copy To Clipboard"))
            ).withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dim.toString())) }, false)
        }
        return 1
    }

    private fun getPlayerWorld(context: CommandContext<ServerCommandSource>): Int {
        val playerWorld = context.source.player.world.registryKey.value
        context.source.sendFeedback(((LiteralText("Player World: ").formatted(Formatting.BLUE).formatted(Formatting.BOLD))).append(((LiteralText("${playerWorld.namespace}:")).formatted(Formatting.GREEN))
            .append((LiteralText(playerWorld.path)).formatted(Formatting.GRAY))).styled { it.withHoverEvent(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT, LiteralText("Copy To Clipboard"))
        ).withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerWorld.toString())) }, false)
        return 1
    }
}
