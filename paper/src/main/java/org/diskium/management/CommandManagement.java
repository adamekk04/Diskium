package org.diskium.management;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandManagement { // TODO: Delete this class and put all methods into individual command classes in commands/

    public static List<LiteralArgumentBuilder<CommandSourceStack>> configs(File dir) {

        List<LiteralArgumentBuilder<CommandSourceStack>> literals = new ArrayList<>();

        Map<String, Object> configs = ConfigManagement.getConfig(dir);
        for (Map.Entry<String, Object> config : configs.entrySet()) {
            literals.add(
                    Commands.literal(config.getKey())
                            .executes(context -> {
                                Object value = ConfigManagement.getSingleConfig(config.getKey());
                                context.getSource().getSender().sendMessage("Configuration '" + config.getKey() + " = " + value + "'");
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(
                                    Commands.argument("value", BoolArgumentType.bool())
                                            .executes(context -> {
                                                boolean value = BoolArgumentType.getBool(context, "value");
                                                if (ConfigManagement.setSingleConfig(config.getKey(), value)) {
                                                    context.getSource().getSender().sendMessage("Set configuration '" + config.getKey() + "' to " + value);
                                                } else {
                                                    context.getSource().getSender().sendMessage("Unable to set configuration '" + config.getKey() + "'");
                                                }
                                                return Command.SINGLE_SUCCESS;
                                            })
                            )
            );
        }
        return literals;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> logsDates(boolean since) {
        if (since) {
            return Commands.literal("since")
                    .then(
                            Commands.argument("sinceArg", StringArgumentType.string())
                                    .executes(context -> {
                                        String inputedDate = StringArgumentType.getString(context, "sinceArg");
                                        LogsManagement.delete(inputedDate, null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(
                                            Commands.literal("until")
                                                    .then(
                                                            Commands.argument("untilArg", StringArgumentType.string())
                                                                    .executes(context -> {
                                                                        String inputedDateSince = StringArgumentType.getString(context, "sinceArg");
                                                                        String inputedDateUntil = StringArgumentType.getString(context, "untilArg");
                                                                        LogsManagement.delete(inputedDateSince, inputedDateUntil);
                                                                        return Command.SINGLE_SUCCESS;
                                                                    })
                                                    )
                                    )
                    );
        } else {
            return Commands.literal("until")
                    .then(
                            Commands.argument("untilArg", StringArgumentType.string())
                                    .executes(context -> {
                                        String inputedDate = StringArgumentType.getString(context, "untilArg");
                                        LogsManagement.delete(null, inputedDate);
                                        return Command.SINGLE_SUCCESS;
                                    })
                    );
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> pluginSwitcher(String literal, boolean enabled, Boolean thisInstance, File dir) {
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

    public static LiteralArgumentBuilder<CommandSourceStack> pluginDeleter(String literal, boolean delPlugin, boolean delFolder, File dir) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal);
        String[] plugins = PluginManagement.getPluginNames(dir);

        for (String pl : plugins) {
            if (delPlugin && !delFolder) {
                root.then(
                        Commands.literal(pl).executes(context -> {
                            PluginManagement.deletePlugin(Bukkit.getPluginManager().getPlugin(pl));
                            return Command.SINGLE_SUCCESS;
                        })
                );
            } else if (!delPlugin && delFolder) {
                if (PluginManagement.hasFolder(pl, dir)) {
                    root.then(
                            Commands.literal(pl).executes(context -> {
                                PluginManagement.deleteFolder(Bukkit.getPluginManager().getPlugin(pl));
                                return Command.SINGLE_SUCCESS;
                            })
                    );
                } // TODO: Edit those two else if blocks to prevent duplicate code
            } else if (delPlugin) {
                if (PluginManagement.hasFolder(pl, dir)) {
                    root.then(
                            Commands.literal(pl).executes(context -> {
                                Plugin plugin = Bukkit.getPluginManager().getPlugin(pl);
                                PluginManagement.deleteFolder(plugin);
                                PluginManagement.deletePlugin(plugin);
                                return Command.SINGLE_SUCCESS;
                            })
                    );
                }
            }
        }

        return root;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> pluginInfo(String literal) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal);
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        for (Plugin pl : plugins) {
            root.then(
                    Commands.literal(pl.getName()).executes(context -> {
                        context.getSource().getSender().sendMessage(PluginManagement.info(pl)); // TODO: Fix possible NPE
                        return Command.SINGLE_SUCCESS;
                    })
            );
        }

        return root;
    }
}
