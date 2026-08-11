package org.diskium.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class MainCommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("diskium")
                        .then(ConfigCommand.entry())
                        .then(LogsCommand.entry())
                        .then(PluginsCommand.entry())
                        .then(WorldCommand.entry())
        );

    }
}
