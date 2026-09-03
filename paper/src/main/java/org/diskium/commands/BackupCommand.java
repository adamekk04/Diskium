package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.TasksUtils;

import java.io.File;

public class BackupCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {

        return Commands.literal("backup")
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    lister(dir, context, null);

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.literal("logs")
                                                .executes(context -> {
                                                    lister(dir, context, "logs");

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("plugins")
                                                .executes(context -> {
                                                    lister(dir, context, "plugins");

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .then(
                                        Commands.literal("world")
                                                .executes(context -> {
                                                    lister(dir, context, "world");

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("remove")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    BackupObj[] backups = TasksUtils.getBackups(dir);

                                                    if (backups != null) TasksUtils.remove(backups[IntegerArgumentType.getInteger(context, "id")], Bukkit.getPluginsFolder());

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("restore")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    BackupObj[] backups = TasksUtils.getBackups(dir);

                                                    if (backups != null) {
                                                        BackupObj backup = backups[IntegerArgumentType.getInteger(context, "id")];
                                                        TasksUtils.add(Bukkit.getPluginsFolder(), new TaskObj(false, backup.getItself() ,backup.getFile(), "backup"));
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }

    private static void lister(File dir, CommandContext<CommandSourceStack> context, String type) {
        int counter = 1;
        boolean all = type == null;
        BackupObj[] backups = TasksUtils.getBackups(dir);

        if (backups != null) {
            if (all) context.getSource().getSender().sendMessage("ID | File | Type");
            else  context.getSource().getSender().sendMessage("ID | File");;
            for (BackupObj backup : backups) {
                if (backup.getType().equalsIgnoreCase(type)) {
                    context.getSource().getSender().sendMessage(counter + "|" + backup.getFile());
                } else {
                    context.getSource().getSender().sendMessage(counter + "|" + backup.getFile() + "|" + backup.getType());
                }
                counter++;
            }
        }
    }
}
