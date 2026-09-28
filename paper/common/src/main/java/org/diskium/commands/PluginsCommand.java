package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.diskium.management.PluginManagement;

public class PluginsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("plugins")
                .then(
                        Commands.literal("disable")
                                .then(
                                        pluginSwitcher("thisInstance", true, true)
                                )
                                .then(
                                        pluginSwitcher("untilManualyEnabled", true, false)
                                )
                )
                .then(
                        pluginSwitcher("enable", false, null)
                )
                .then(
                        Commands.literal("delete")
                                .then(
                                        pluginDeleter("folder", false, true)
                                )
                                .then(
                                        pluginDeleter("plugin", true, false)
                                )
                                .then(
                                        pluginDeleter("both", true, true)
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
                                                    TextComponent info = PluginManagement.info(pl);

                                                    if (info != null) {
                                                        context.getSource().getSender().sendMessage(info);
                                                    } else {
                                                        context.getSource().getSender().sendMessage(Component.text("Something went wrong while obtaining plugin info", NamedTextColor.RED));
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

                                    context.getSource().getSender().sendMessage(Component.text("Found ")
                                            .append(Component.text(plugins.length, NamedTextColor.DARK_GREEN))
                                            .append(Component.text(plugins.length == 1 ? " plugin" : " plugins")));

                                    for (Plugin pl : plugins) {
                                        context.getSource().getSender().sendMessage(Component.text(pl.getName(), NamedTextColor.GREEN));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pluginSwitcher(String literal, boolean enabled, Boolean thisInstance) {
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
                                        if (PluginManagement.tempDisablePlugin(pl)) {
                                            context.getSource().getSender().sendMessage(Component.text("Plugin ")
                                                    .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(" disabled")));
                                        } else {
                                            context.getSource().getSender().sendMessage(Component.text("Plugin ", NamedTextColor.RED)
                                                    .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(" couldn't be disabled", NamedTextColor.RED)));
                                        }
                                    } else if (enabled) {
                                        if (PluginManagement.permDisablePlugin(pl)) {
                                            context.getSource().getSender().sendMessage(Component.text("Plugin ")
                                                    .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(" disabled")));
                                        } else {
                                            context.getSource().getSender().sendMessage(Component.text("Plugin ", NamedTextColor.RED)
                                                    .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(" couldn't be disabled", NamedTextColor.RED)));
                                        }
                                    } else {
                                        if (!PluginManagement.tempEnablePlugin(pl)) {
                                            if (!PluginManagement.permEnablePlugin(pl)) {
                                                context.getSource().getSender().sendMessage(Component.text("Plugin ", NamedTextColor.RED)
                                                        .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                        .append(Component.text(" couldn't be enabled", NamedTextColor.RED)));
                                            } else {
                                                context.getSource().getSender().sendMessage(Component.text("Plugin ")
                                                        .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                        .append(Component.text(" enabled")));
                                            }
                                        } else {
                                            context.getSource().getSender().sendMessage(Component.text("Plugin ")
                                                    .append(Component.text(pl.getName(), NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(" enabled")));
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pluginDeleter(String literal, boolean delPlugin, boolean delFolder) {
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

                                    if (PluginManagement.del(pl, delPlugin, delFolder)) {
                                        context.getSource().getSender().sendMessage("Deletion was successful.");
                                    } else {
                                        context.getSource().getSender().sendMessage(Component.text("Something went wrong while deleting.", NamedTextColor.RED));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }
}
