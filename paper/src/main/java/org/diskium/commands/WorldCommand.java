package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
                                .then(buildChecker(Commands.literal("wholeWorld"), Checker.WHOLE_WORLD, false))
                                .then(sector(Commands.literal("region"), false))
                                .then(sector(Commands.literal("chunk"), true))
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
        return root
                .then(
                        buildChecker(Commands.argument("radius", IntegerArgumentType.integer(1)), Checker.RANGE, in)
                ).then(
                        buildChecker(Commands.literal("border"), Checker.BORDER, in)
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> sector(LiteralArgumentBuilder<CommandSourceStack> root, boolean isChunk) {
        return root.then(
                buildChecker(Commands.argument("coords", ArgumentTypes.blockPosition()), Checker.SECTOR, isChunk)
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildChecker(RequiredArgumentBuilder<CommandSourceStack, ?> root, Checker checker, boolean in) {
        return root.then(
                        Commands.literal("checkForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.RANGE) {
                                        WorldManagement.del(context.getArgument("world", World.class), in, IntegerArgumentType.getInteger(context, "radius"), true);
                                    } else if (checker == Checker.SECTOR) {
                                        BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());
                                        World world = context.getArgument("world", World.class);
                                        int x = blockPosition.blockX();
                                        int z = blockPosition.blockZ();

                                        WorldManagement.delSector(x, z, in, true, world);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        Commands.literal("dontCheckForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.RANGE) {
                                        WorldManagement.del(context.getArgument("world", World.class), in, IntegerArgumentType.getInteger(context, "radius"), false);
                                    } else if (checker == Checker.SECTOR) {
                                        BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());
                                        World world = context.getArgument("world", World.class);
                                        int x = blockPosition.blockX();
                                        int z = blockPosition.blockZ();

                                        WorldManagement.delSector(x, z, in, false, world);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildChecker(LiteralArgumentBuilder<CommandSourceStack> root, Checker checker, boolean in) {
        return root.then(
                        Commands.literal("checkForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.BORDER) {
                                        World world = context.getArgument("world", World.class);

                                        WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), true);
                                    } else if (checker == Checker.WHOLE_WORLD) {
                                        WorldManagement.del(context.getArgument("world", World.class), true);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        Commands.literal("dontCheckForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.BORDER) {
                                        World world = context.getArgument("world", World.class);

                                        WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), false);
                                    } else if (checker == Checker.WHOLE_WORLD) {
                                        WorldManagement.delWorld(context.getArgument("world", World.class));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    enum Checker {
        RANGE,
        BORDER,
        WHOLE_WORLD,
        SECTOR
    }
}
