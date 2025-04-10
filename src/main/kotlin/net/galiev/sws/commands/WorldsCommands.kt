package net.galiev.sws.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.galiev.sws.config.Config
import net.galiev.sws.config.ConfigManager
import net.galiev.sws.config.ExactSpawn
import net.galiev.sws.config.RangeSpawn
import net.galiev.sws.helper.WorldHelper
import net.galiev.sws.helper.WorldHelper.dims
import net.minecraft.command.argument.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.*

object WorldsCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("sws")
                .then(CommandManager.literal("worlds").executes { getAllWorlds(it) })
                .then(CommandManager.literal("playerWorld").executes { getPlayerWorld(it) })
                .then(
                    CommandManager.literal("teleport")
                        .requires { it.hasPermissionLevel(2) }
                        .then(
                            CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                .then(
                                    CommandManager.argument("targets", EntityArgumentType.players())
                                        .then(
                                            CommandManager.argument("location", Vec3ArgumentType.vec3())
                                                .executes {
                                                    teleportToDimension(
                                                        it,
                                                        EntityArgumentType.getPlayers(it, "targets"),
                                                        DimensionArgumentType.getDimensionArgument(it, "dimension"),
                                                        Vec3ArgumentType.getPosArgument(it, "location"),
                                                        null
                                                    )
                                                }
                                                .then(
                                                    CommandManager.argument("rotation", RotationArgumentType.rotation())
                                                        .executes {
                                                            teleportToDimension(
                                                                it,
                                                                EntityArgumentType.getPlayers(it, "targets"),
                                                                DimensionArgumentType.getDimensionArgument(it, "dimension"),
                                                                Vec3ArgumentType.getPosArgument(it, "location"),
                                                                RotationArgumentType.getRotation(it, "rotation")
                                                            )
                                                        }
                                                )
                                        )
                                )
                        )
                )
                .then(
                    CommandManager.literal("setspawndimension")
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
                .append((Text.literal(dim.path)).formatted(Formatting.GRAY))).styled { it.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, Text.literal("Copy To Clipboard"))
            ).withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dim.toString())) }}, false)
        }
        return 1
    }

    private fun getPlayerWorld(context: CommandContext<ServerCommandSource>): Int {
        val playerWorld = context.source.player?.world?.registryKey?.value
        context.source.sendFeedback({((Text.literal("Player World: ").formatted(Formatting.BLUE).formatted(Formatting.BOLD))).append(((Text.literal("${playerWorld?.namespace}:")).formatted(Formatting.GREEN))
            .append((Text.literal(playerWorld?.path)).formatted(Formatting.GRAY))).styled { it.withHoverEvent(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Text.literal("Copy To Clipboard"))
        ).withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerWorld.toString())) }}, false)
        return 1
    }

    private fun setSpawnWorldSpawn(
        context: CommandContext<ServerCommandSource>,
        serverWorld: ServerWorld,
        pos: BlockPos,
        angle: Float
    ): Int {
        if (dims.contains(serverWorld.registryKey.value)) {
            context.source.world.players.forEach {
                if (!WorldHelper.hasRespawnBlocks(it)) {
                    it.setSpawnPoint(serverWorld.registryKey, pos, angle, true, false)
                }
            }
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
            context.source.sendFeedback({
                Text.translatable(
                    "commands.setworldspawn.success",
                    pos.x,
                    pos.y,
                    pos.z,
                    serverWorld.registryKey.value.toString()
                )
            }, true)
            return 1
        } else {
            context.source.sendError(Text.literal("World not exist"))
            return 0
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun teleportToDimension(
        context: CommandContext<ServerCommandSource>,
        targets: Collection<Entity?>,
        serverWorld: ServerWorld,
        location: PosArgument,
        rotation: PosArgument?
    ): Int {
        val pos = location.toAbsolutePos(context.source)
        val vec2f = rotation?.toAbsoluteRotation(context.source)

        for (entity in targets) {
            val flags = getFlags(location, rotation, entity?.world?.registryKey == serverWorld.registryKey)
            if (vec2f == null) {
                teleport(
                    entity!!,
                    serverWorld,
                    pos.x,
                    pos.y,
                    pos.z,
                    flags,
                    entity.yaw,
                    entity.pitch
                )
            } else {
                teleport(
                    entity!!,
                    serverWorld,
                    pos.x,
                    pos.y,
                    pos.z,
                    flags,
                    vec2f.y,
                    vec2f.x
                )
            }
        }

        if (targets.size == 1) {
            context.source.sendFeedback(
                {
                    Text.translatable(
                        "commands.teleport.success.location.single",
                        (targets.iterator().next() as Entity).displayName,
                        serverWorld.registryKey.value.toString(),
                        "${formatFloat(pos.x)}, ${formatFloat(pos.y)}",
                        formatFloat(pos.z)
                    )
                },
                true
            )
        } else {
            context.source.sendFeedback(
                {
                    Text.translatable(
                        "commands.teleport.success.location.multiple",
                        targets.size,
                        serverWorld.registryKey.value.toString(),
                        "${formatFloat(pos.x)}, ${formatFloat(pos.y)}",
                        formatFloat(pos.z)
                    )
                },
                true
            )
        }

        return targets.size
    }

    private fun getFlags(pos: PosArgument, rotation: PosArgument?, sameDimension: Boolean): Set<PositionFlag> {
        val set: MutableSet<PositionFlag> = EnumSet.noneOf(PositionFlag::class.java)
        if (pos.isXRelative) {
            set.add(PositionFlag.X)
            if (sameDimension) {
                set.add(PositionFlag.X)
            }
        }

        if (pos.isYRelative) {
            set.add(PositionFlag.Y)
            if (sameDimension) {
                set.add(PositionFlag.Y)
            }
        }

        if (pos.isZRelative) {
            set.add(PositionFlag.Z)
            if (sameDimension) {
                set.add(PositionFlag.Z)
            }
        }

        if (rotation == null || rotation.isXRelative) {
            set.add(PositionFlag.X_ROT)
        }

        if (rotation == null || rotation.isYRelative) {
            set.add(PositionFlag.Y_ROT)
        }

        return set
    }

    private fun formatFloat(d: Double): String = String.format(Locale.ROOT, "%f", d)

    @Throws(CommandSyntaxException::class)
    private fun teleport(
        target: Entity,
        world: ServerWorld,
        x: Double,
        y: Double,
        z: Double,
        movementFlags: Set<PositionFlag>,
        yaw: Float,
        pitch: Float
    ) {
        val blockPos = BlockPos.ofFloored(x, y, z)
        if (!World.isValid(blockPos)) {
            throw SimpleCommandExceptionType(Text.translatable("commands.teleport.invalidPosition")).create()
        } else {
            val d = if (movementFlags.contains(PositionFlag.X)) x - target.x else x
            val e = if (movementFlags.contains(PositionFlag.Y)) y - target.y else y
            val f = if (movementFlags.contains(PositionFlag.Z)) z - target.z else z
            val g = if (movementFlags.contains(PositionFlag.Y_ROT)) yaw - target.yaw else yaw
            val h = if (movementFlags.contains(PositionFlag.X_ROT)) pitch - target.pitch else pitch
            val i = MathHelper.wrapDegrees(g)
            val j = MathHelper.wrapDegrees(h)
            if (target.teleport(world, d, e, f, movementFlags, i, j)) {

                if (!(target is LivingEntity && target.isFallFlying)) {
                    target.velocity = target.velocity.multiply(1.0, 0.0, 1.0)
                    target.isOnGround = true
                }

                if (target is PathAwareEntity) {
                    target.navigation.stop()
                }
            }
        }
    }
}
