package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.diskium.management.WorldManagement;

public class WorldCommand { // TODO: Make method for only check/don't check for builds

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("world");

        return root.then(worldArgSeparator(Commands.literal("allWorlds")))
                .then(worldArgSeparator(Commands.argument("world", ArgumentTypes.world())));
    }

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> T worldArgSeparator(T root) {
        return root.then(
                        Commands.literal("getBlock")
                                .then(blockCoords(Commands.literal("thisWorld"), true))
                                .then(blockCoords(Commands.literal("naturally"), false))
                )
                .then(
                        Commands.literal("info")
                                .executes(context -> {
                                    context.getSource().getSender().sendMessage(WorldManagement.info(context.getArgument("world", World.class)));
                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        Commands.literal("delete")
                                .then(range(Commands.literal("in"), true))
                                .then(range(Commands.literal("out"), false))
                                .then(wholeWorld(Commands.literal("wholeWorld")))
                                .then(sector(Commands.literal("region"), false))
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> blockCoords(LiteralArgumentBuilder<CommandSourceStack> root, boolean existing) {
        return root.then(
                Commands.argument("position", ArgumentTypes.blockPosition())
                        .executes(context -> {
                            BlockPosition blockPosition = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());
                            World world = context.getArgument("world", World.class);
                            Location loc = blockPosition.toLocation(world);

                            context.getSource().getSender().sendMessage(WorldManagement.getBlock(loc, existing));
                            return Command.SINGLE_SUCCESS;
                        })
        );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> range(LiteralArgumentBuilder<CommandSourceStack> root, boolean in) {
        return root.then(
                Commands.argument("radius", IntegerArgumentType.integer(1))
                        .then(
                                Commands.literal("checkForBuilds")
                                        .executes(context -> {
                                            WorldManagement.del(context.getArgument("world", World.class), in, IntegerArgumentType.getInteger(context, "radius"), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .then(
                                Commands.literal("dontCheckForBuilds")
                                        .executes(context -> {
                                            WorldManagement.del(context.getArgument("world", World.class), in, IntegerArgumentType.getInteger(context, "radius"), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                        ))
                .then(
                        Commands.literal("border")
                                .then(
                                        Commands.literal("checkForBuilds")
                                                .executes(context -> {
                                                    World world = context.getArgument("world", World.class);

                                                    WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), true);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("dontCheckForBuilds")
                                                .executes(context -> {
                                                    World world = context.getArgument("world", World.class);

                                                    WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), false);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> wholeWorld(LiteralArgumentBuilder<CommandSourceStack> root) {
        return root.then(
                Commands.literal("checkForBuilds")
                        .executes(context -> {
                            WorldManagement.del(context.getArgument("world", World.class), true);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(
                                Commands.literal("dontCheckForBuilds")
                                        .executes(context -> {
                                            WorldManagement.delWorld(context.getArgument("world", World.class));
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> sector(LiteralArgumentBuilder<CommandSourceStack> root, boolean isChunk) {
        return root.then(
                Commands.argument("coords", ArgumentTypes.blockPosition())
                        .then(
                                Commands.literal("checkForBuilds")
                                        .executes(context -> {
                                            BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());
                                            World world = context.getArgument("world", World.class);
                                            int x = blockPosition.blockX();
                                            int z = blockPosition.blockZ();

                                            WorldManagement.delSector(x, z, isChunk, true, world);

                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .then(
                                Commands.literal("dontCheckForBuilds")
                                        .executes(context -> {
                                            BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());
                                            World world = context.getArgument("world", World.class);
                                            int x = blockPosition.blockX();
                                            int z = blockPosition.blockZ();

                                            WorldManagement.delSector(x, z, isChunk, false, world);

                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );
    }
}
