package org.diskium.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class MainCommand {

    public static LiteralCommandNode<CommandSourceStack> register() {

        return Commands.literal("diskium")
                .then(ConfigCommand.entry())
                .then(LogsCommand.entry())
                .then(PluginsCommand.entry())
                .then(WorldCommand.entry())
                .then(TaskCommand.entry())
                .build();
    }
}
