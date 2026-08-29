package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.diskium.management.CommandManagement;

import java.io.File;

public class PluginsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {

        return Commands.literal("plugins")
                .then(
                        Commands.literal("disable")
                                .then(
                                        CommandManagement.pluginSwitcher("thisInstance", true, true, dir)
                                )
                                .then(
                                        CommandManagement.pluginSwitcher("untilManualyEnabled", true, false, dir)
                                )
                )
                .then(
                        CommandManagement.pluginSwitcher("enable", false, null, dir)
                )
                .then(
                        Commands.literal("delete")
                                .then(
                                        CommandManagement.pluginDeleter("folder", false, true)
                                )
                                .then(
                                        CommandManagement.pluginDeleter("plugin", true, false)
                                )
                                .then(
                                        CommandManagement.pluginDeleter("both", true, true)
                                )
                )
                .then(
                        Commands.literal("info")
                        // TODO: list all plugins as literals
                )
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
                                    context.getSource().getSender().sendMessage("Found " + plugins.length + " plugins:");
                                    for (Plugin pl : plugins) {
                                        context.getSource().getSender().sendMessage(pl.getName());
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }
}
