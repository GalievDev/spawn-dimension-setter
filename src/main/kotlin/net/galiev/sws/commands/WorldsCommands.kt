package net.galiev.sws.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.galiev.sws.config.Config
import net.galiev.sws.config.ConfigManager
import net.galiev.sws.config.ExactSpawn
import net.galiev.sws.config.RangeSpawn
import net.galiev.sws.helper.WorldHelper.dims
import net.minecraft.command.argument.AngleArgumentType
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.DimensionArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

object WorldsCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>){
        dispatcher.register(
            CommandManager.literal("sws")
                .then(CommandManager.literal("worlds").executes { getAllWorlds(it) })
                .then(CommandManager.literal("playerWorld").executes { getPlayerWorld(it) })
                .then(CommandManager.literal("setspawnworld")
                    .requires { it.hasPermissionLevel(2) }
                    .executes {
                        setSpawnWorldSpawn(
                            it,
                            it.source.world,
                            BlockPos.ofFloored(it.source.position),
                            0.0F
                        )
                    }
                    .then(
                        CommandManager.argument("dimension", DimensionArgumentType.dimension())
                            .executes {
                                setSpawnWorldSpawn(
                                    it,
                                    DimensionArgumentType.getDimensionArgument(it, "dimension"),
                                    BlockPos.ofFloored(it.source.position),
                                    0.0F
                                )
                            }
                            .then(
                                CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                    .executes {
                                        setSpawnWorldSpawn(
                                            it,
                                            DimensionArgumentType.getDimensionArgument(it, "dimension"),
                                            BlockPosArgumentType.getValidBlockPos(it, "pos"),
                                            0.0F
                                        )
                                    }
                            )
                            .then(
                                CommandManager.argument("angle", AngleArgumentType.angle())
                                    .executes {
                                        setSpawnWorldSpawn(
                                            it,
                                            DimensionArgumentType.getDimensionArgument(it, "dimension"),
                                            BlockPosArgumentType.getValidBlockPos(it, "pos"),
                                            AngleArgumentType.getAngle(it, "angle")
                                        )
                                    }
                            )
                    )
                )
        )
    }

    private fun getAllWorlds(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback({ Text.literal("All Worlds in your Minecraft: ").styled{it.withColor(Formatting.BLUE).withBold(true)}}, false)
        for (dim in dims) {
            context.source.sendFeedback({((Text.literal(" - "))).append(((Text.literal("${dim.namespace}:")).formatted(Formatting.GREEN))
                .append((Text.literal(dim.path)).formatted(Formatting.GRAY))).styled {
                    it.withHoverEvent(HoverEvent.ShowText(Text.literal("Copy To Clipboard")))
                        .withClickEvent(ClickEvent.CopyToClipboard(dim.toString())) }}, false)
        }
        return 1
    }

    private fun getPlayerWorld(context: CommandContext<ServerCommandSource>): Int {
        val playerWorld = context.source.player?.world?.registryKey?.value
        context.source.sendFeedback({((Text.literal("Player World: ").formatted(Formatting.BLUE).formatted(Formatting.BOLD))).append(((Text.literal("${playerWorld?.namespace}:")).formatted(Formatting.GREEN))
            .append((Text.literal(playerWorld?.path)).formatted(Formatting.GRAY))).styled {
                it.withHoverEvent(HoverEvent.ShowText(Text.literal("Copy To Clipboard")))
                    .withClickEvent(ClickEvent.CopyToClipboard(playerWorld.toString())) }}, false)
        return 1
    }

    private fun setSpawnWorldSpawn(context: CommandContext<ServerCommandSource>, serverWorld: ServerWorld, pos: BlockPos, angle: Float): Int {
        if (dims.contains(serverWorld.registryKey.value)) {
            serverWorld.setSpawnPos(pos, angle)
            ConfigManager.write(
                Config(
                    serverWorld.registryKey.value.toString(),
                    safeCheck = false,
                    isRangeSpawn = false,
                    isExactSpawn = true,
                    RangeSpawn(0, 0),
                    ExactSpawn(pos.x, pos.y, pos.z)
                )
            )
            context.source.sendFeedback ({
                Text.translatable("commands.setworldspawn.success", serverWorld.registryKey.value.toString(), pos.x, pos.y, pos.z, angle)
            }, false)
            return 1
        } else {
            context.source.sendError(Text.literal("World not exist"))
            return 0
        }
    }
}
