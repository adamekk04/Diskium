package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.diskium.BackupObj;
import org.diskium.TaskObj;
import org.diskium.TasksUtils;

public class BackupCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("backup")
                .then(
                        Commands.literal("list") // TODO: Merge all list subcommands to prevent duped code
                                .executes(context -> {
                                    context.getSource().getSender().sendMessage("ID | File | Type");
                                    int counter = 1;
                                    for (BackupObj backup : TasksUtils.getBackups(Bukkit.getPluginsFolder())) {
                                        context.getSource().getSender().sendMessage(counter + "|" + backup.getFile() + "|" + backup.getType());
                                        counter++;
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.literal("logs")
                                                .executes(context -> {
                                                    context.getSource().getSender().sendMessage("ID | File");
                                                    int counter = 1;
                                                    for (BackupObj backup : TasksUtils.getBackups(Bukkit.getPluginsFolder())) {
                                                        if (backup.getType().equalsIgnoreCase("logs")) {
                                                            context.getSource().getSender().sendMessage(counter + "|" + backup.getFile());
                                                            counter++;
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("plugins")
                                                .executes(context -> {
                                                    context.getSource().getSender().sendMessage("ID | File");
                                                    int counter = 1;
                                                    for (BackupObj backup : TasksUtils.getBackups(Bukkit.getPluginsFolder())) {
                                                        if (backup.getType().equalsIgnoreCase("plugins")) {
                                                            context.getSource().getSender().sendMessage(counter + "|" + backup.getFile());
                                                            counter++;
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("world")
                                                .executes(context -> {
                                                    context.getSource().getSender().sendMessage("ID | File");
                                                    int counter = 1;
                                                    for (BackupObj backup : TasksUtils.getBackups(Bukkit.getPluginsFolder())) {
                                                        if (backup.getType().equalsIgnoreCase("world")) {
                                                            context.getSource().getSender().sendMessage(counter + "|" + backup.getFile());
                                                            counter++;
                                                        }
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("remove")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(1, TasksUtils.getBackups(Bukkit.getPluginsFolder()).length))
                                                .executes(context -> {
                                                    TasksUtils.remove(TasksUtils.getBackups(Bukkit.getPluginsFolder())[IntegerArgumentType.getInteger(context, "id")]);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("restore")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(1, TasksUtils.getBackups(Bukkit.getPluginsFolder()).length))
                                                .executes(context -> {
                                                    BackupObj backup = TasksUtils.getBackups(Bukkit.getPluginsFolder())[IntegerArgumentType.getInteger(context, "id")];
                                                    TasksUtils.add(Bukkit.getPluginsFolder(), new TaskObj(false, backup.getItself() ,backup.getFile(), "backup"));

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }
}
