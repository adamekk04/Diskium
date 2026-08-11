package org.diskium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class WorldCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("world");
    }
}
