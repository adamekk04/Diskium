package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.diskium.management.PluginManagement;

public class PluginsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("plugins")
                .then(
                        Commands.literal("disable")
                                .then(
                                        Commands.literal("thisInstance")
                                        // list all enabled plugins as literals
                                )
                                .then(
                                        Commands.literal("untilManuallyEnabled")
                                        // list all enabled plugins as literals
                                )
                )
                .then(
                        Commands.literal("enable")
                        // list all disabled plugins as literals
                )
                .then(
                        Commands.literal("delete")
                                .then(
                                        Commands.literal("folder")
                                        // list all plugins with folders as literals
                                )
                                .then(
                                        Commands.literal("plugin")
                                        // list all plugins as literals
                                )
                                .then(
                                        Commands.literal("both")
                                        // list all plugins as literals
                                )
                )
                .then(
                        Commands.literal("info")
                        // list all plugins as literals
                )
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    String[] plugins = PluginManagement.getPlugins();
                                    context.getSource().getSender().sendMessage("Found " + plugins.length + " plugins:");
                                    for (String pl : plugins) {
                                        context.getSource().getSender().sendMessage(pl);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }
}
