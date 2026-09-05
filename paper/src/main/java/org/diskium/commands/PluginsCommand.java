package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.diskium.management.PluginManagement;

import java.io.File;

public class PluginsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {

        return Commands.literal("plugins")
                .then(
                        Commands.literal("disable")
                                .then(
                                        pluginSwitcher("thisInstance", true, true, dir)
                                )
                                .then(
                                        pluginSwitcher("untilManualyEnabled", true, false, dir)
                                )
                )
                .then(
                        pluginSwitcher("enable", false, null, dir)
                )
                .then(
                        Commands.literal("delete")
                                .then(
                                        pluginDeleter("folder", false, true, dir)
                                )
                                .then(
                                        pluginDeleter("plugin", true, false, dir)
                                )
                                .then(
                                        pluginDeleter("both", true, true, dir)
                                )
                )
                .then(
                        Commands.literal("info")
                                .then(
                                        Commands.argument("plugin", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

                                                    for (Plugin plugin : plugins) {
                                                        builder.suggest(plugin.getName());
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    Plugin pl = Bukkit.getPluginManager().getPlugin(StringArgumentType.getString(context, "plugin"));
                                                    String info = PluginManagement.info(pl);

                                                    if (info != null) {
                                                        context.getSource().getSender().sendMessage(info);
                                                    } else {
                                                        context.getSource().getSender().sendMessage("Something went wrong while obtaining plugin info");
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
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

    private static LiteralArgumentBuilder<CommandSourceStack> pluginSwitcher(String literal, boolean enabled, Boolean thisInstance, File dir) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal);
        String[] plugins = PluginManagement.getPluginNames(dir);

        for (String pl : plugins) {
            if (enabled && thisInstance) {
                root.then(
                        Commands.literal(pl).executes(context -> {
                            PluginManagement.tempDisablePlugin(Bukkit.getPluginManager().getPlugin(pl));
                            return Command.SINGLE_SUCCESS;
                        })
                );
            } else if (enabled) {
                root.then(
                        Commands.literal(pl).executes(context -> {
                            PluginManagement.permDisablePlugin(Bukkit.getPluginManager().getPlugin(pl));
                            return Command.SINGLE_SUCCESS;
                        })
                );
            } else {
                root.then(
                        Commands.literal(pl).executes(context -> {
                            Plugin plugin = Bukkit.getPluginManager().getPlugin(pl);

                            if (!PluginManagement.tempEnablePlugin(plugin)) {
                                PluginManagement.permEnablePlugin(plugin);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
            }
        }

        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pluginDeleter(String literal, boolean delPlugin, boolean delFolder, File dir) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal);
        String[] plugins = PluginManagement.getPluginNames(dir);

        for (String pl : plugins) {
            root.then(
                    Commands.literal(pl).executes(context -> {
                        PluginManagement.del(Bukkit.getPluginManager().getPlugin(pl), delPlugin, delFolder);

                        return Command.SINGLE_SUCCESS;
                    })
            );
        }

        return root;
    }
}
