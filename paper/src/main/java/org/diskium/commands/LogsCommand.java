package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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
                                    context.getSource().getSender().sendMessage("Found " + logs.length + " logs");
                                    for (File log : logs) {
                                        context.getSource().getSender().sendMessage(log.getName());
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
                                    context.getSource().getSender().sendMessage("Found " + logs.length + " logs, deleting them all");
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
                                .executes(context -> {
                                    context.getSource().getSender().sendMessage("You need to enter keywords (/diskium logs search <keywords>)");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("keywords", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String keywords = StringArgumentType.getString(context, "keywords");
                                                    Map<File, Integer> results = LogsManagement.search(keywords);
                                                    for (Map.Entry<File, Integer> entry : results.entrySet()) {
                                                        File key = entry.getKey();
                                                        int value = entry.getValue();

                                                        context.getSource().getSender().sendMessage("Found " + value + " matches in " + key.getName());
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
