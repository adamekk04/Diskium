package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.diskium.management.LogsManagement;
import org.diskium.management.PluginManagement;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.Flags;
import org.diskium.utils.TasksUtils;

import java.io.File;
import java.util.Map;

public class DeleteCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {
        return Commands.literal("delete")
                .then(Commands.literal("backup")
                        .then(backups()))
                .then(Commands.literal("logs")
                        .executes(context -> {
                            logsExec(context);

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("plugins")
                        .then(DeleteCommand.pluginDeleter())
                .then(Commands.literal("tasks")
                        .then(tasks()))
                .then(
                        Commands.literal("world")
                                .then(WorldCommand.worldArgSeparator(Commands.literal("allWorlds"), true))
                                .then(WorldCommand.worldArgSeparator(Commands.argument("world", ArgumentTypes.world()), false))
                ));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, ?> backups() {
        return Commands.argument("id", IntegerArgumentType.integer(1))
                .suggests((context, builder) -> {
                    for (int i = 1; i <= TasksUtils.getBackups().length; i++) {
                        builder.suggest(i);
                    }

                    return builder.buildFuture();
                })
                .executes(context -> {
                    BackupObj[] backups = TasksUtils.getBackups();
                    int arg = IntegerArgumentType.getInteger(context, "id") - 1;

                    if (backups != null && arg < backups.length) {
                        TasksUtils.remove(backups[arg]);
                        context.getSource().getSender().sendMessage(Component.text("Deleted backup ")
                                .append(Component.text(arg + 1, NamedTextColor.DARK_GREEN)));
                    } else {
                        context.getSource().getSender().sendMessage(Component.text("Backup with id ")
                                .append(Component.text((arg + 1), NamedTextColor.DARK_GREEN))
                                .append(Component.text(" doesn't exist.")));
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }

    public static void logsExec(CommandContext<CommandSourceStack> context) {
        File[] logs = LogsManagement.getLogs(null, null);

        context.getSource().getSender().sendMessage(
                Component.text("Found ")
                        .append(Component.text(logs.length, NamedTextColor.DARK_GREEN))
                        .append(Component.text(logs.length == 1 ? " log, " : " logs, "))
                        .append(Component.text("DELETING!", NamedTextColor.RED))
        );

        LogsManagement.delete(null, null);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, ?> pluginDeleter() {
        return Commands.argument("plugin", StringArgumentType.word())
                .suggests((context, builder) -> {
                    for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                        builder.suggest(plugin.getName());
                    }

                    return builder.buildFuture();
                })
                .then(
                        Commands.argument("flags", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    Flags flags = new Flags(Map.of("jarFile", boolean.class, "folder", boolean.class));
                                    String[] suggestions = flags.suggest(builder.getRemainingLowerCase());

                                    for (String suggestion : suggestions) {
                                        builder.suggest(suggestion);
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Plugin pl = Bukkit.getPluginManager().getPlugin(StringArgumentType.getString(context, "plugin"));
                                    Flags flags = new Flags(Map.of("jarFile", boolean.class, "folder", boolean.class));
                                    flags.suggest(StringArgumentType.getString(context, "flags"));

                                    boolean delPlugin = false;
                                    boolean delFolder = false;

                                    for (Map.Entry<String, Object> entry : flags.getAll().entrySet()) {
                                        if (entry.getKey().equalsIgnoreCase("jarFile")) {
                                            delPlugin = Boolean.getBoolean(entry.getValue().toString());
                                        } else {
                                            delFolder = Boolean.getBoolean(entry.getValue().toString());
                                        }
                                    }

                                    if (PluginManagement.del(pl, delPlugin, delFolder)) {
                                        context.getSource().getSender().sendMessage("Deletion was successful.");
                                    } else {
                                        context.getSource().getSender().sendMessage(Component.text("Something went wrong while deleting.", NamedTextColor.RED));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    public static RequiredArgumentBuilder<CommandSourceStack, ?> tasks() {
        return Commands.argument("id", IntegerArgumentType.integer(0))
                .suggests((context, builder) -> {
                    for (int i = 0; i < TasksUtils.getTasks().length; i++) {
                        builder.suggest(i);
                    }

                    return builder.buildFuture();
                })
                .executes(context -> {
                    int index = IntegerArgumentType.getInteger(context, "id");
                    TaskObj[] tasks = TasksUtils.getTasks();

                    if (index <= tasks.length) {
                        TasksUtils.remove(tasks[index]);
                        context.getSource().getSender().sendMessage(Component.text("Deleted task with id")
                                .append(Component.text(index, NamedTextColor.GREEN)));
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }

    public static LiteralArgumentBuilder<CommandSourceStack> world(LiteralArgumentBuilder<CommandSourceStack> root, boolean all) {
        return root.then(WorldCommand.range(Commands.literal("in"), true, all))
                .then(WorldCommand.range(Commands.literal("out"), false, all))
                .then(WorldCommand.buildChecker(Commands.literal("wholeWorld"), WorldCommand.Checker.WHOLE_WORLD, false, all))
                .then(WorldCommand.sector(Commands.literal("region"), false, all))
                .then(WorldCommand.sector(Commands.literal("chunk"), true, all));
    }
}
