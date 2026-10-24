package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.diskium.management.WorldManagement;
import org.diskium.objects.DependingFlagObj;
import org.diskium.objects.FlagObj;
import org.diskium.utils.Flags;

import java.util.List;
import java.util.Map;

public class WorldCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> entry() {
        return Commands.literal("world")
                .then(
                        Commands.literal("getBlock").then(getBlock(true)).then(getBlock(false))
                )
                .then(
                        Commands.literal("info")
                                .executes(context -> {
                                    List<World> worlds = Bukkit.getWorlds();
                                    for (World world : worlds) {
                                        context.getSource().getSender().sendMessage(WorldManagement.info(world));
                                        if (worlds.getLast() != world) {
                                            context.getSource().getSender().sendMessage("-----");
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("world", ArgumentTypes.world())
                                                .executes(context -> {
                                                    World world = context.getArgument("world", World.class);

                                                    context.getSource().getSender().sendMessage(WorldManagement.info(world));

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(Commands.literal("delete")
                        .then(delete(true))
                        .then(delete(false)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> getBlock(boolean singleWorld) {
        if (singleWorld) {
            return Commands.argument("world", ArgumentTypes.world()).then(getBlockSeparator(true));
        }

        return Commands.literal("all").then(getBlockSeparator(false));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> getBlockSeparator(boolean singleWorld) {
        return Commands.argument("coords", ArgumentTypes.blockPosition())
                .executes(context -> {
                    BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());

                    if (singleWorld) {
                        Location location = blockPosition.toLocation(context.getArgument("world", World.class));
                        WorldManagement.getBlock(location, false).thenAccept(context.getSource().getSender()::sendMessage);
                    } else {
                        for (World world : Bukkit.getWorlds()) {
                            Location location = blockPosition.toLocation(world);
                            WorldManagement.getBlock(location, false).thenAccept(context.getSource().getSender()::sendMessage);
                        }
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .then(
                        Commands.argument("flags", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    Flags flags = new Flags(
                                            new FlagObj("naturally", true, false)
                                    );
                                    String typed = builder.getRemaining();
                                    String[] suggestions = flags.suggest(typed);

                                    int flagStart = typed.lastIndexOf("--");
                                    int equals = typed.lastIndexOf('=');
                                    int lastSpace = typed.lastIndexOf(' ');

                                    int offset = equals > flagStart ? equals + 1 : Math.max(0, flagStart);
                                    if (offset < lastSpace) {
                                        offset = lastSpace + 1;
                                    }
                                    SuggestionsBuilder completion = builder.createOffset(builder.getStart() + offset);

                                    for (String suggestion : suggestions) {
                                        completion.suggest(suggestion);
                                    }

                                    return completion.buildFuture();
                                })
                                .executes(context -> {
                                    Flags flags = new Flags(
                                            new FlagObj("naturally", true, false)
                                    );
                                    flags.suggest(StringArgumentType.getString(context, "flags"));
                                    boolean naturally = Boolean.parseBoolean(flags.getAll().get("naturally").toString());

                                    BlockPosition blockPosition = context.getArgument("coords", BlockPositionResolver.class).resolve(context.getSource());

                                    if (singleWorld) {
                                        Location location = blockPosition.toLocation(context.getArgument("world", World.class));
                                        WorldManagement.getBlock(location, naturally).thenAccept(context.getSource().getSender()::sendMessage);
                                        context.getSource().getSender().sendMessage(Component.text("Generating new world, it may take a while!", NamedTextColor.GOLD));
                                    } else {
                                        context.getSource().getSender().sendMessage(Component.text("Generating new worlds, it may take a while!", NamedTextColor.GOLD));
                                        for (World world : Bukkit.getWorlds()) {
                                            Location location = blockPosition.toLocation(world);
                                            WorldManagement.getBlock(location, naturally).thenAccept(context.getSource().getSender()::sendMessage);
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    public static ArgumentBuilder<CommandSourceStack, ?> delete(boolean singleWorld) {
        if (singleWorld) {
            return Commands.argument("world", ArgumentTypes.world()).then(deleteSeparator(true));
        }

        return Commands.literal("all").then(deleteSeparator(false));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> deleteSeparator(boolean singleWorld) {
        return Commands.argument("flags", StringArgumentType.greedyString())
                .suggests((context, builder) -> {
                    Flags flags = new Flags(
                            new FlagObj<>("checker", "range", "border", "whole_world", "chunk", "region"),
                            new FlagObj<>("checkForBuilds", true, false),
                            new DependingFlagObj<Integer>("radius", "checker", new String[]{"range"}),
                            new DependingFlagObj<Integer>("x", "checker", new String[]{"chunk", "region"}),
                            new DependingFlagObj<Integer>("z", "checker", new String[]{"chunk", "region"}),
                            new DependingFlagObj<>("in", "checker", new String[]{"range"}, true, false)
                    );
                    String typed = builder.getRemaining();
                    String[] suggestions = flags.suggest(typed);

                    int flagStart = typed.lastIndexOf("--");
                    int equals = typed.lastIndexOf('=');
                    int lastSpace = typed.lastIndexOf(' ');

                    int offset = equals > flagStart ? equals + 1 : Math.max(0, flagStart);
                    if (offset < lastSpace) {
                        offset = lastSpace + 1;
                    }
                    SuggestionsBuilder completion = builder.createOffset(builder.getStart() + offset);

                    for (String suggestion : suggestions) {
                        completion.suggest(suggestion);
                    }

                    return completion.buildFuture();
                })
                .executes(context -> {
                    Flags flags = new Flags(
                            new FlagObj<>("checker", "range", "border", "whole_world", "chunk", "region"),
                            new FlagObj<>("checkForBuilds", true, false),
                            new DependingFlagObj<Integer>("radius", "checker", new String[]{"range"}),
                            new DependingFlagObj<Integer>("x", "checker", new String[]{"chunk", "region"}),
                            new DependingFlagObj<Integer>("z", "checker", new String[]{"chunk", "region"}),
                            new DependingFlagObj<>("in", "checker", new String[]{"range"}, true, false)
                    );
                    flags.suggest(StringArgumentType.getString(context, "flags"));

                    if (singleWorld) {
                        World world = context.getArgument("world", World.class);
                        deleteLogic(world, flags);
                    } else {
                        for (World world : Bukkit.getWorlds()) {
                            deleteLogic(world, flags);
                        }
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static void deleteLogic(World world, Flags flags) {
        Map<String, Object> map = flags.getAll();
        String checker = map.get("checker").toString();
        boolean checkForBuilds = Boolean.parseBoolean(map.get("checkForBuilds").toString());

        if (checker.equalsIgnoreCase("border")) {
            WorldManagement.del(
                    world,
                    true,
                    (int) world.getWorldBorder().getSize() / 2,
                    checkForBuilds,
                    world.getWorldBorder().getCenter()
            );
        } else if (checker.equalsIgnoreCase("whole_world")) {
            WorldManagement.del(world, checkForBuilds);
        } else if (checker.equalsIgnoreCase("range")) {
            WorldManagement.del(
                    world,
                    Boolean.parseBoolean(flags.getAll().get("in").toString()),
                    Integer.parseInt(map.get("range").toString()),
                    checkForBuilds,
                    new Location(world, 0, 0, 0)
            );
        } else if (checker.equalsIgnoreCase("chunk")) {
            WorldManagement.delSector(
                    Integer.parseInt(map.get("x").toString()),
                    Integer.parseInt(map.get("z").toString()),
                    true,
                    checkForBuilds,
                    world
            );
        } else if (checker.equalsIgnoreCase("region")) {
            WorldManagement.delSector(
                    Integer.parseInt(map.get("x").toString()),
                    Integer.parseInt(map.get("z").toString()),
                    false,
                    checkForBuilds,
                    world
            );
        }
    }
}
