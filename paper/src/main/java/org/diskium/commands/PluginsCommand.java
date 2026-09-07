package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
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
        return Commands.literal(literal)
                .then(
                        Commands.argument("plugin", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
                                        builder.suggest(pl.getName());
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Plugin pl = Bukkit.getPluginManager().getPlugin(StringArgumentType.getString(context, "plugin"));

                                    if (enabled && thisInstance) {
                                        PluginManagement.tempDisablePlugin(pl);
                                    } else if (enabled) {
                                        PluginManagement.permDisablePlugin(pl);
                                    } else {
                                        if (!PluginManagement.tempEnablePlugin(pl)) {
                                            PluginManagement.permEnablePlugin(pl);
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pluginDeleter(String literal, boolean delPlugin, boolean delFolder, File dir) {
        return Commands.literal(literal)
                .then(
                        Commands.argument("plugin", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                                        builder.suggest(plugin.getName());
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Plugin pl = Bukkit.getPluginManager().getPlugin(context.getArgument("plugin", String.class));

                                    PluginManagement.del(pl, delPlugin, delFolder);

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }
}
