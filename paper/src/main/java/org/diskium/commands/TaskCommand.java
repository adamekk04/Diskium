package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.diskium.Diskium;
import org.diskium.objects.TaskObj;
import org.diskium.utils.TasksUtils;

import java.io.File;
import java.util.Arrays;

public class TaskCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {
        return Commands.literal("task")
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    lister(null, context);

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.literal("logs")
                                                .executes(context -> {
                                                    lister("Logs", context);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("plugins")
                                                .executes(context -> {
                                                    lister("Plugins", context);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("world")
                                                .executes(context -> {
                                                    lister("World", context);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("remove")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(0))
                                                .suggests((context, builder) -> {
                                                    for (int i = 0; i < TasksUtils.getTasks(dir).length; i++) {
                                                        builder.suggest(i);
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    int index = IntegerArgumentType.getInteger(context, "id");
                                                    TaskObj[] tasks = TasksUtils.getTasks(dir);
                                                    if (tasks != null) {
                                                        if (tasks.length >= index)
                                                            TasksUtils.remove(tasks[index], Diskium.getInstance().getDataFolder());
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("info")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(0))
                                                .suggests((context, builder) -> {
                                                    for (int i = 0; i < TasksUtils.getTasks(dir).length; i++) {
                                                        builder.suggest(i);
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    TaskObj task;
                                                    int index = IntegerArgumentType.getInteger(context, "id");
                                                    TaskObj[] tasks = TasksUtils.getTasks(dir);
                                                    if (tasks != null) {
                                                        if (tasks.length >= index) {
                                                            task = tasks[index];
                                                            context.getSource().getSender().sendMessage("ID: " + index);
                                                            context.getSource().getSender().sendMessage("Type: " + task.getType());
                                                            context.getSource().getSender().sendMessage("Delete: " + task.getDelete());
                                                            context.getSource().getSender().sendMessage("Path: " + task.getFile().toPath());
                                                            if (!task.getDelete())
                                                                context.getSource().getSender().sendMessage("Replacement path: " + task.getReplacementFile().toPath());
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }

    private static void lister(File dir, String type, CommandContext<CommandSourceStack> context) {
        TaskObj[] tasks = Arrays.stream(TasksUtils.getBackups(dir)).filter(task -> task.getType().equalsIgnoreCase(type) || type == null).toArray(TaskObj[]::new);

        context.getSource().getSender().sendMessage(Component.text("Found ")
                .append(Component.text(tasks.length, NamedTextColor.DARK_GREEN))
                .append(Component.text(tasks.length == 1 ? " task" : " tasks")));

        if (tasks.length == 0) return;

        int longestID = Math.max(Integer.toString(tasks.length).length(), 2);
        int longestFile = 4;
        int longestReplacementFile = 16;
        int longestType = type == null ? 0 : 4;

        for (TaskObj task : tasks) {
            if (task.getFile().toString().length() > longestFile) {
                longestFile = task.getFile().toString().length();
            }

            if (task.getReplacementFile().toString().length() > longestReplacementFile) {
                longestReplacementFile = task.getReplacementFile().toString().length();
            }

            if (type != null) {
                if (task.getType().length() > longestType) {
                    longestType = task.getType().length();
                }
            }
        }

        TextComponent header = Component.text("ID" + " ".repeat(longestID - 2), NamedTextColor.DARK_GREEN)
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text("Delete", NamedTextColor.DARK_GREEN)) // longest boolean (false) is shorter than "delete"
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text("File" + " ".repeat(longestFile - 4), NamedTextColor.DARK_GREEN))
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text("Replacement File" + " ".repeat(longestReplacementFile - 16), NamedTextColor.DARK_GREEN));

        if (type != null) {
            header = header.append(Component.text(" | ", NamedTextColor.WHITE)
                    .append(Component.text("Type" + " ".repeat(longestType - 4), NamedTextColor.DARK_GREEN)));
        }

        context.getSource().getSender().sendMessage(header);

        for (int i = 0; i < tasks.length; i++) {
            TextComponent text = Component.text((i + 1) + " ".repeat(longestID - (i + 1)), NamedTextColor.DARK_GREEN)
                    .append(Component.text(" | ", NamedTextColor.WHITE))
                    .append(Component.text(tasks[i].getFile().toString() + " ".repeat(longestFile - (i + 1)), NamedTextColor.DARK_GREEN))
                    .append(Component.text(" | ", NamedTextColor.WHITE))
                    .append(Component.text(tasks[i].getReplacementFile().toString() + " ".repeat(longestReplacementFile - (i + 1)), NamedTextColor.DARK_GREEN))
                    .append(Component.text(" | ", NamedTextColor.WHITE));

            if (type != null) {
                text = text.append(Component.text(" | ", NamedTextColor.WHITE))
                        .append(Component.text(type + " ".repeat(longestType - (i + 1)), NamedTextColor.DARK_GREEN));
            }

            context.getSource().getSender().sendMessage(text);
        }
    }
}