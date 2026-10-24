package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.diskium.management.PluginManagement;
import org.diskium.objects.FlagObj;
import org.diskium.utils.Flags;

import java.util.Map;

public class PluginsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("plugins")
                .then(
                        Commands.literal("disable")
                                .then(
                                        Commands.argument("plugin", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

                                                    for (Plugin plugin : plugins) {
                                                        builder.suggest(plugin.toString());
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    Plugin plugin = Bukkit.getPluginManager().getPlugin(StringArgumentType.getString(context, "plugin"));
                                                    pluginDisabler(plugin, true);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(
                                                        Commands.argument("flags", StringArgumentType.greedyString())
                                                                .suggests((context, builder) -> {
                                                                    Flags flags = new Flags(
                                                                            new FlagObj<>("persistentlyDisabled", true, false)
                                                                    );
                                                                    String typed = builder.getRemaining();
                                                                    String[] suggestions = flags.suggest(typed);

                                                                    int flagStart = typed.lastIndexOf("--");
                                                                    int equals = typed.lastIndexOf('=');
                                                                    int lastSpace = typed.lastIndexOf(' ');

                                                                    int offset = equals > flagStart ? equals + 1 : Math.max(0, flagStart);
                                                                    if (offset < lastSpace) {
                                                                        offset = lastSpace + 1;
                                                                    }
                                                                    SuggestionsBuilder completion = builder.createOffset(builder.getStart() + offset);

                                                                    for (String suggestion : suggestions) {
                                                                        completion.suggest(suggestion);
                                                                    }

                                                                    return completion.buildFuture();
                                                                })
                                                                .executes(context -> {
                                                                    Plugin plugin = Bukkit.getPluginManager().getPlugin(StringArgumentType.getString(context, "plugin"));
                                                                    Flags flags = new Flags(
                                                                            new FlagObj<>("persistentlyDisabled", true, false)
                                                                    );
                                                                    flags.suggest(StringArgumentType.getString(context, "flags"));
                                                                    boolean thisInstance = true;

                                                                    for (Map.Entry<String, Object> entry : flags.getAll().entrySet()) {
                                                                        if (entry.getKey().equalsIgnoreCase("persistentlyDisabled")) {
                                                                            thisInstance = !Boolean.getBoolean(entry.getValue().toString());
                                                                        }
                                                                    }

                                                                    pluginDisabler(plugin, thisInstance);

                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                )
                                )
                )
                .then(pluginEnabler())
                .then(Commands.literal("delete").then(DeleteCommand.pluginDeleter()))
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

    private static void pluginDisabler(Plugin plugin, boolean thisInstance) {
        if (thisInstance) {
            PluginManagement.tempDisablePlugin(plugin);
        } else {
            PluginManagement.permDisablePlugin(plugin);
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pluginEnabler() {
        return Commands.literal("enable")
                .then(
                        Commands.argument("plugin", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

                                    for (Plugin plugin : plugins) {
                                        builder.suggest(plugin.toString());
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Plugin plugin = Bukkit.getPluginManager().getPlugin(StringArgumentType.getString(context, "plugin"));
                                    PluginManagement.permEnablePlugin(plugin);

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }
}
