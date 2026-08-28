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

public class TaskCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> entry() {
        return Commands.literal("task")
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    TaskObj[] taskObjs = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                    context.getSource().getSender().sendMessage("ID|Type|Delete|Path|ReplacementPath");

                                    for (int i = 0; i < taskObjs.length; i++) {
                                        context.getSource().getSender().sendMessage(i + "|" + taskObjs[i].getType() + "|" + taskObjs[i].getDelete() + "|" + taskObjs[i].getFile() + "|" + taskObjs[i].getReplacementFile());
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.literal("logs")
                                                .executes(context -> { // TODO: Prevent exec duplicating code
                                                    TaskObj[] taskObjs = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                                    context.getSource().getSender().sendMessage("ID|Delete|Path|ReplacementPath");

                                                    for (int i = 0; i < taskObjs.length; i++) {
                                                        if (taskObjs[i].getType().equalsIgnoreCase("Log")) {
                                                            context.getSource().getSender().sendMessage(i + "|" + taskObjs[i].getDelete() + "|" + taskObjs[i].getFile() + "|" + taskObjs[i].getReplacementFile());
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("plugins")
                                                .executes(context -> {
                                                    TaskObj[] taskObjs = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                                    context.getSource().getSender().sendMessage("ID|Delete|Path|ReplacementPath");

                                                    for (int i = 0; i < taskObjs.length; i++) {
                                                        if (taskObjs[i].getType().equalsIgnoreCase("Plugin")) {
                                                            context.getSource().getSender().sendMessage(i + "|" + taskObjs[i].getDelete() + "|" + taskObjs[i].getFile() + "|" + taskObjs[i].getReplacementFile());
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("world")
                                                .executes(context -> {
                                                    TaskObj[] taskObjs = TasksUtils.getTasks(Bukkit.getPluginsFolder());
                                                    context.getSource().getSender().sendMessage("ID|Delete|Path|ReplacementPath");

                                                    for (int i = 0; i < taskObjs.length; i++) {
                                                        if (taskObjs[i].getType().equalsIgnoreCase("World")) {
                                                            context.getSource().getSender().sendMessage(i + "|" + taskObjs[i].getDelete() + "|" + taskObjs[i].getFile() + "|" + taskObjs[i].getReplacementFile());
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("remove")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(0, TasksUtils.getTasks(Bukkit.getPluginsFolder()).length))
                                                .executes(context -> {
                                                    TasksUtils.remove(TasksUtils.getTasks(Bukkit.getPluginsFolder())[IntegerArgumentType.getInteger(context, "id")]);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("info")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(0, TasksUtils.getTasks(Bukkit.getPluginsFolder()).length))
                                                .executes(context -> {
                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    TaskObj task = TasksUtils.getTasks(Bukkit.getPluginsFolder())[id];

                                                    context.getSource().getSender().sendMessage("ID: " + id);
                                                    context.getSource().getSender().sendMessage("Type: " + task.getType());
                                                    context.getSource().getSender().sendMessage("Delete: " + task.getDelete());
                                                    context.getSource().getSender().sendMessage("Path: " + task.getFile().toPath());
                                                    if (!task.getDelete()) context.getSource().getSender().sendMessage("Replacement path: " + task.getReplacementFile().toPath());

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }
}