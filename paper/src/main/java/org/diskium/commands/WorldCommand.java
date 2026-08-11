package org.diskium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class WorldCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("world")
                .then(
                        Commands.literal("allWorlds")
                );
    }
}
