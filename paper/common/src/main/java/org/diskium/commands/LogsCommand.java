package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.diskium.management.LogsManagement;import org.diskium.objects.FlagObj;import org.diskium.utils.DateUtils;import org.diskium.utils.Flags;

import java.io.File;
import java.time.LocalDate;import java.util.Map;

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
                                        Commands.argument("flags", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> {
                                                    Flags flags = new Flags(
                                                            new FlagObj<String>("since"),
                                                            new FlagObj<String>("until")
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
                                                    Flags flags = new Flags(
                                                            new FlagObj<String>("since"),
                                                            new FlagObj<String>("until")
                                                    );
                                                    String arg = StringArgumentType.getString(context, "flags");
                                                    flags.suggest(arg);

                                                    LocalDate since = null;
                                                    LocalDate until = null;

                                                    for (Map.Entry<String, Object> entry : flags.getAll().entrySet()) {
                                                        if (entry.getKey().equalsIgnoreCase("since")) {
                                                            since = DateUtils.parse(entry.getValue().toString());
                                                        } else {
                                                            until = DateUtils.parse(entry.getValue().toString());
                                                        }
                                                    }

                                                    File[] logs = LogsManagement.getLogs(since, until);

                                                    context.getSource().getSender().sendMessage(
                                                            Component.text("Found ")
                                                                    .append(Component.text(logs.length, NamedTextColor.DARK_GREEN))
                                                                    .append(Component.text(logs.length == 1 ? " log" : " logs")));

                                                    for (File log : logs) {
                                                        context.getSource().getSender().sendMessage(Component.text(log.getName(), NamedTextColor.GREEN));
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("delete")
                                .executes(context -> {
                                    DeleteCommand.logsExec(context);

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("flags", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> {
                                                    Flags flags = new Flags(
                                                            new FlagObj<String>("since"),
                                                            new FlagObj<String>("until")
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
                                                    Flags flags = new Flags(
                                                            new FlagObj<String>("since"),
                                                            new FlagObj<String>("until")
                                                    );
                                                    String arg = StringArgumentType.getString(context, "flags");
                                                    flags.suggest(arg);

                                                    LocalDate since = null;
                                                    LocalDate until = null;

                                                    for (Map.Entry<String, Object> entry : flags.getAll().entrySet()) {
                                                        if (entry.getKey().equalsIgnoreCase("since")) {
                                                            since = DateUtils.parse(entry.getValue().toString());
                                                        } else {
                                                            until = DateUtils.parse(entry.getValue().toString());
                                                        }
                                                    }

                                                    DeleteCommand.logsExec(context, since, until);

                                                    return Command.SINGLE_SUCCESS;
                                                })
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

                                                        if (value != 0) {
                                                            context.getSource().getSender().sendMessage(
                                                                    Component.text("Found ")
                                                                            .append(Component.text(value, NamedTextColor.DARK_GREEN))
                                                                            .append(Component.text(" matches in "))
                                                                            .append(Component.text(key.getName(), NamedTextColor.GREEN))
                                                            );
                                                        }
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }
}
