package org.diskium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.diskium.management.CommandManagement;

import java.util.List;

public class ConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        List<LiteralArgumentBuilder<CommandSourceStack>> subcommands = CommandManagement.configs();
        LiteralArgumentBuilder<CommandSourceStack> configRoot = Commands.literal("config");

        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : subcommands) {
            configRoot.then(subcommand);
        }

        return configRoot;
    }
}
