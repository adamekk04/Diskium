package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.diskium.management.LogsManagement;

import java.io.File;
import java.util.Map;

public class LogsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("logs")
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    File[] logs = LogsManagement.getLogs(null, null);

                                    context.getSource().getSender().sendMessage(
                                            Component.text("Found ")
                                                    .append(Component.text(logs.length, NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(logs.length == 1 ? " log" : " logs")));

                                    for (File log : logs) {
                                        context.getSource().getSender().sendMessage(Component.text(log.getName(), NamedTextColor.GREEN));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        logsDates(true)
                                )
                                .then(
                                        logsDates(false)
                                )
                )
                .then(
                        Commands.literal("delete")
                                .executes(context -> {
                                    File[] logs = LogsManagement.getLogs(null, null);

                                    context.getSource().getSender().sendMessage(
                                            Component.text("Found ")
                                                    .append(Component.text(logs.length, NamedTextColor.DARK_GREEN))
                                                    .append(Component.text(logs.length == 1 ? " log, " : " logs, "))
                                                    .append(Component.text("DELETING!", NamedTextColor.RED))
                                    );

                                    LogsManagement.delete(null, null);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        logsDates(true)
                                )
                                .then(
                                        logsDates(false)
                                )
                )
                .then(
                        Commands.literal("search")
                                .then(
                                        Commands.argument("keywords", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String keywords = StringArgumentType.getString(context, "keywords");
                                                    Map<File, Integer> results = LogsManagement.search(keywords);

                                                    for (Map.Entry<File, Integer> entry : results.entrySet()) {
                                                        File key = entry.getKey();
                                                        int value = entry.getValue();

                                                        context.getSource().getSender().sendMessage(
                                                                Component.text("Found ")
                                                                        .append(Component.text(value, NamedTextColor.DARK_GREEN))
                                                                        .append(Component.text(" matches in "))
                                                                        .append(Component.text(key.getName(), NamedTextColor.GREEN))
                                                        );
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> logsDates(boolean since) {
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
}
