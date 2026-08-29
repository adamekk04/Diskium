package org.diskium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.diskium.management.CommandManagement;

import java.io.File;
import java.util.List;

public class ConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {

        List<LiteralArgumentBuilder<CommandSourceStack>> subcommands = CommandManagement.configs(dir);
        LiteralArgumentBuilder<CommandSourceStack> configRoot = Commands.literal("config");

        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : subcommands) {
            configRoot.then(subcommand);
        }

        return configRoot;
    }
}
