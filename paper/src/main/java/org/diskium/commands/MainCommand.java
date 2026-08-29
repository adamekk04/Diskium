package org.diskium.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.io.File;

public class MainCommand {

    public static LiteralCommandNode<CommandSourceStack> register(File dir) {

        return Commands.literal("diskium")
                .then(ConfigCommand.entry(dir))
                .then(LogsCommand.entry())
                .then(PluginsCommand.entry(dir))
                .then(WorldCommand.entry())
                .then(TaskCommand.entry())
                .then(BackupCommand.entry())
                .build();
    }
}
