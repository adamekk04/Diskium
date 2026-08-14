package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.diskium.management.WorldManagement;

public class WorldCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("world");

        return root
                .then(
                        Commands.literal("allWorlds")
                )
                .then(
                        Commands.argument("world", ArgumentTypes.world())
                                .then(
                                        Commands.literal("getBlock")
                                                .then(
                                                        Commands.literal("thisWorld")
                                                                .then(
                                                                        Commands.argument("position", ArgumentTypes.blockPosition())
                                                                                .executes(context -> {
                                                                                    BlockPosition blockPosition = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());
                                                                                    World world = context.getArgument("world", World.class);
                                                                                    Location loc = blockPosition.toLocation(world);

                                                                                    context.getSource().getSender().sendMessage(WorldManagement.getBlock(loc, true));
                                                                                    return Command.SINGLE_SUCCESS;
                                                                                })
                                                                )
                                                )
                                                .then(
                                                        Commands.literal("naturally")
                                                                .then(
                                                                        Commands.argument("position", ArgumentTypes.blockPosition())
                                                                                .executes(context -> {
                                                                                    BlockPosition blockPosition = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());
                                                                                    World world = context.getArgument("world", World.class);
                                                                                    Location loc = blockPosition.toLocation(world);

                                                                                    context.getSource().getSender().sendMessage(WorldManagement.getBlock(loc, false));
                                                                                    return Command.SINGLE_SUCCESS;
                                                                                })
                                                                )
                                                )
                                )
                                .then(
                                        Commands.literal("info")
                                                .executes(context -> {
                                                    context.getSource().getSender().sendMessage(WorldManagement.info(context.getArgument("world", World.class)));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }
}
