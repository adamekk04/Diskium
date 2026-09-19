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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.diskium.management.WorldManagement;

public class WorldCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("world");

        return root.then(worldArgSeparator(Commands.literal("allWorlds"), true))
                .then(worldArgSeparator(Commands.argument("world", ArgumentTypes.world()), false));
    }

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> T worldArgSeparator(T root, boolean all) {
        return root.then(
                        Commands.literal("getBlock")
                                .then(blockCoords(Commands.literal("thisWorld"), true, all))
                                .then(blockCoords(Commands.literal("naturally"), false, all))
                )
                .then(
                        Commands.literal("info")
                                .executes(context -> {
                                    if (all) {
                                        for (World world : Bukkit.getWorlds()) {
                                            context.getSource().getSender().sendMessage(WorldManagement.info(world));
                                        }
                                    } else {
                                        context.getSource().getSender().sendMessage(WorldManagement.info(context.getArgument("world", World.class)));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        Commands.literal("delete")
                                .then(range(Commands.literal("in"), true, all))
                                .then(range(Commands.literal("out"), false, all))
                                .then(buildChecker(Commands.literal("wholeWorld"), Checker.WHOLE_WORLD, false, all))
                                .then(sector(Commands.literal("region"), false, all))
                                .then(sector(Commands.literal("chunk"), true, all))
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> blockCoords(LiteralArgumentBuilder<CommandSourceStack> root, boolean existing, boolean all) {
        return root.then(
                Commands.argument("position", ArgumentTypes.blockPosition())
                        .executes(context -> {
                            BlockPosition blockPosition = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());
                            if (all) {
                                for (World world : Bukkit.getWorlds()) {
                                    Location loc = blockPosition.toLocation(world);
                                    context.getSource().getSender().sendMessage(WorldManagement.getBlock(loc, existing));
                                }
                            } else {
                                Location loc = blockPosition.toLocation(context.getArgument("world", World.class));
                                context.getSource().getSender().sendMessage(WorldManagement.getBlock(loc, existing));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
        );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> range(LiteralArgumentBuilder<CommandSourceStack> root, boolean in, boolean all) {
        return root
                .then(
                        buildChecker(Commands.argument("radius", IntegerArgumentType.integer(1)), Checker.RANGE, in, all)
                ).then(
                        buildChecker(Commands.literal("border"), Checker.BORDER, in, all)
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> sector(LiteralArgumentBuilder<CommandSourceStack> root, boolean isChunk, boolean all) {
        return root.then(
                buildChecker(Commands.argument("coords", ArgumentTypes.blockPosition()), Checker.SECTOR, isChunk, all)
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildChecker(RequiredArgumentBuilder<CommandSourceStack, ?> root, Checker checker, boolean in, boolean all) {
        return root.then(
                        Commands.literal("checkForBuilds")
                                .executes(context -> {
                                        if (checker == Checker.RANGE) {
                                            if (all) {
                                                for (World world : Bukkit.getWorlds()) {
                                                    WorldManagement.del(world, in, IntegerArgumentType.getInteger(context, "radius"), true);
                                                }
                                            } else {
                                                WorldManagement.del(context.getArgument("world", World.class), in, IntegerArgumentType.getInteger(context, "radius"), true);
                                            }
                                        } else if (checker == Checker.SECTOR) {
                                            BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());
                                            int x = blockPosition.blockX();
                                            int z = blockPosition.blockZ();

                                            if (all) {
                                                for (World world : Bukkit.getWorlds()) {
                                                    WorldManagement.delSector(x, z, in, true, world);
                                                }
                                            } else {
                                                WorldManagement.delSector(x, z, in, true, context.getArgument("world", World.class));
                                            }
                                        }

                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        Commands.literal("dontCheckForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.RANGE) {
                                        if (all) {
                                            for (World world : Bukkit.getWorlds()) {
                                                WorldManagement.del(world, in, IntegerArgumentType.getInteger(context, "radius"), false);
                                            }
                                        } else {
                                            WorldManagement.del(context.getArgument("world", World.class), in, IntegerArgumentType.getInteger(context, "radius"), false);
                                        }
                                    } else if (checker == Checker.SECTOR) {
                                        BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());
                                        int x = blockPosition.blockX();
                                        int z = blockPosition.blockZ();

                                        if (all) {
                                            for (World world : Bukkit.getWorlds()) {
                                                WorldManagement.delSector(x, z, in, false, world);
                                            }
                                        } else {
                                            World world = context.getArgument("world", World.class);
                                            WorldManagement.delSector(x, z, in, false, world);
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildChecker(LiteralArgumentBuilder<CommandSourceStack> root, Checker checker, boolean in, boolean all) {
        return root.then(
                        Commands.literal("checkForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.BORDER) {
                                        if (all) {
                                            for (World world : Bukkit.getWorlds()) {
                                                WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), true);
                                            }
                                        } else {
                                            World world = context.getArgument("world", World.class);
                                            WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), true);
                                        }
                                    } else if (checker == Checker.WHOLE_WORLD) {
                                        if (all) {
                                            for (World world : Bukkit.getWorlds()) {
                                                WorldManagement.del(world, true);
                                            }
                                        } else {
                                            WorldManagement.del(context.getArgument("world", World.class), true);
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                )
                .then(
                        Commands.literal("dontCheckForBuilds")
                                .executes(context -> {
                                    if (checker == Checker.BORDER) {
                                        if (all) {
                                            for (World world : Bukkit.getWorlds()) {
                                                WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), false);
                                            }
                                        } else {
                                            World world = context.getArgument("world", World.class);
                                            WorldManagement.del(world, in, (int) world.getWorldBorder().getSize(), false);
                                        }
                                    } else if (checker == Checker.WHOLE_WORLD) {
                                        if (all) {
                                            for (World world : Bukkit.getWorlds()) {
                                                WorldManagement.delWorld(world);
                                            }
                                        } else {
                                            WorldManagement.delWorld(context.getArgument("world", World.class));
                                        }
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
