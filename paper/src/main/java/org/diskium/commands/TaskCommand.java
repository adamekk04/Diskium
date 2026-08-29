package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sun.jdi.IntegerType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.diskium.TaskObj;
import org.diskium.TasksUtils;

import java.io.File;

public class TaskCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {
        return Commands.literal("task")
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    TaskObj[] tasks = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                    if (tasks != null) {
                                        context.getSource().getSender().sendMessage("ID|Type|Delete|Path|ReplacementPath");

                                        for (int i = 0; i < tasks.length; i++) {
                                            context.getSource().getSender().sendMessage(i + "|" + tasks[i].getType() + "|" + tasks[i].getDelete() + "|" + tasks[i].getFile() + "|" + tasks[i].getReplacementFile());
                                        }
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.literal("logs")
                                                .executes(context -> { // TODO: Prevent exec duplicating code
                                                    TaskObj[] tasks = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                                    if (tasks != null) {
                                                        context.getSource().getSender().sendMessage("ID|Delete|Path|ReplacementPath");

                                                        for (int i = 0; i < tasks.length; i++) {
                                                            if (tasks[i].getType().equalsIgnoreCase("Log")) {
                                                                context.getSource().getSender().sendMessage(i + "|" + tasks[i].getDelete() + "|" + tasks[i].getFile() + "|" + tasks[i].getReplacementFile());
                                                            }
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("plugins")
                                                .executes(context -> {
                                                    TaskObj[] tasks = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                                    if (tasks != null) {
                                                        context.getSource().getSender().sendMessage("ID|Delete|Path|ReplacementPath");

                                                        for (int i = 0; i < tasks.length; i++) {
                                                            if (tasks[i].getType().equalsIgnoreCase("Plugin")) {
                                                                context.getSource().getSender().sendMessage(i + "|" + tasks[i].getDelete() + "|" + tasks[i].getFile() + "|" + tasks[i].getReplacementFile());
                                                            }
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("world")
                                                .executes(context -> {
                                                    TaskObj[] tasks = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                                    if (tasks != null) {
                                                        context.getSource().getSender().sendMessage("ID|Delete|Path|ReplacementPath");

                                                        for (int i = 0; i < tasks.length; i++) {
                                                            if (tasks[i].getType().equalsIgnoreCase("World")) {
                                                                context.getSource().getSender().sendMessage(i + "|" + tasks[i].getDelete() + "|" + tasks[i].getFile() + "|" + tasks[i].getReplacementFile());
                                                            }
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("remove")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    int index = IntegerArgumentType.getInteger(context, "id");
                                                    TaskObj[] tasks = TasksUtils.getTasks(dir);
                                                    if (tasks != null) {
                                                        if (tasks.length >= index) TasksUtils.remove(tasks[index]);
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("info")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(0))
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
                                                            if (!task.getDelete()) context.getSource().getSender().sendMessage("Replacement path: " + task.getReplacementFile().toPath());
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }
}