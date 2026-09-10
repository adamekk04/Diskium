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
import org.bukkit.Bukkit;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.TasksUtils;

import java.io.File;
import java.util.Arrays;

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
                                        Commands.argument("id", IntegerArgumentType.integer())
                                                .suggests((context, builder) -> {
                                                    for (int i = 0; i < TasksUtils.getTasks(dir).length; i++) {
                                                        builder.suggest(i);
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    BackupObj[] backups = TasksUtils.getBackups(dir);

                                                    if (backups != null) {
                                                        TasksUtils.remove(backups[IntegerArgumentType.getInteger(context, "id") - 1], Bukkit.getPluginsFolder());
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("restore")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer())
                                                .suggests((context, builder) -> {
                                                    for (int i = 0; i < TasksUtils.getTasks(dir).length; i++) {
                                                        builder.suggest(i);
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    BackupObj[] backups = TasksUtils.getBackups(dir);

                                                    if (backups != null) {
                                                        BackupObj backup = backups[IntegerArgumentType.getInteger(context, "id") - 1];
                                                        TasksUtils.add(Bukkit.getPluginsFolder(), new TaskObj(false, backup.getItself(), backup.getFile(), "backup"));
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
            context.getSource().getSender().sendMessage(Component.text("Found ")
                    .append(Component.text(backups.length, NamedTextColor.DARK_GREEN))
                    .append(Component.text(" backups")));
            if (backups.length == 0 || Arrays.stream(backups).filter(backupObj -> backupObj.getType().equalsIgnoreCase(type)).toArray(BackupObj[]::new).length == 0 && !all) return;

            context.getSource().getSender().sendMessage(tableHeader(all));

            for (BackupObj backup : backups) {
                TextComponent component = backupOutput(all, backup, type, counter);

                if (component != null) {
                    context.getSource().getSender().sendMessage(component);
                }

                counter++;
            }
        }
    }

    private static TextComponent tableHeader(boolean all) {
        TextComponent component = Component.text("ID", NamedTextColor.DARK_GREEN)
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text("File", NamedTextColor.DARK_GREEN));
        if (all) {
            return component.append(Component.text(" | ", NamedTextColor.WHITE))
                    .append(Component.text("Type", NamedTextColor.DARK_GREEN));
        }
        return component;
    }

    private static TextComponent backupOutput(boolean all, BackupObj backup, String type, int counter) {
        TextComponent component = Component.text(counter, NamedTextColor.GREEN)
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text(backup.getFile().toString(), NamedTextColor.GREEN));

        if (all) {
            return component.append(Component.text(" | ", NamedTextColor.WHITE))
                    .append(Component.text(backup.getType(), NamedTextColor.GREEN));
        }

        if (backup.getType().equalsIgnoreCase(type)) {
            return component;
        }

        return null;
    }
}
