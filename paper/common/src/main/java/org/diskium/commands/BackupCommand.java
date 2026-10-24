package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.diskium.objects.BackupObj;
import org.diskium.objects.FlagObj;
import org.diskium.objects.TaskObj;
import org.diskium.utils.Flags;
import org.diskium.utils.TasksUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BackupCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() {

        return Commands.literal("backup")
                .then(
                        Commands.literal("list")
                                .executes(context -> {
                                    lister(context, null);

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("flags", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> {
                                                    Flags flags = new Flags(
                                                            new FlagObj<>("logs", true, false),
                                                            new FlagObj<>("plugins", true, false),
                                                            new FlagObj<>("world", true, false)
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
                                                            new FlagObj<>("logs", true, false),
                                                            new FlagObj<>("plugins", true, false),
                                                            new FlagObj<>("world", true, false)
                                                    );
                                                    String arg = StringArgumentType.getString(context, "flags");
                                                    List<TaskObj.Types> types = new ArrayList<>();
                                                    flags.suggest(arg);

                                                    for (Map.Entry<String, Object> entry : flags.getAll().entrySet()) {
                                                        if (Boolean.parseBoolean(entry.getValue().toString())) {
                                                            types.add(TaskObj.Types.valueOf(entry.getKey()));
                                                        }
                                                    }

                                                    lister(context, types);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("delete")
                                .then(DeleteCommand.backups()))
                .then(
                        Commands.literal("restore")
                                .then(
                                        Commands.argument("id", IntegerArgumentType.integer(1))
                                                .suggests((context, builder) -> {
                                                    for (int i = 0; i < TasksUtils.getBackups().length; i++) {
                                                        builder.suggest(i);
                                                    }

                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    BackupObj[] backups = TasksUtils.getBackups();
                                                    int index = IntegerArgumentType.getInteger(context, "id");

                                                    if (index <= backups.length) {
                                                        BackupObj backup = backups[index - 1];
                                                        TasksUtils.add(new TaskObj(false, backup.getItself(), backup.getFile(), TaskObj.Types.BACKUP));
                                                    } else {
                                                        context.getSource().getSender().sendMessage(Component.text("Backup with id ")
                                                                .append(Component.text(index, NamedTextColor.DARK_GREEN))
                                                                .append(Component.text(" doesn't exist")));
                                                    }

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }

    private static void lister(CommandContext<CommandSourceStack> context, List<TaskObj.Types> types) {
        int counter = 1;
        BackupObj[] backups = Arrays.stream(TasksUtils.getBackups())
                .filter(backup -> types == null || types.contains(backup.getType()))
                .toArray(BackupObj[]::new);

        context.getSource().getSender().sendMessage(Component.text("Found ")
                .append(Component.text(backups.length, NamedTextColor.DARK_GREEN))
                .append(Component.text(backups.length == 1 ? " backup" : " backups")));

        if (backups.length == 0) return;

        int idLength = Math.max(String.valueOf(backups.length).length(), 2);
        int backupLength = 4; // "File".length()

        for (BackupObj backup : backups) {
            int tempBackupLen = backup.getFile().toString().length();

            if (tempBackupLen > backupLength) {
                backupLength = tempBackupLen;
            }
        }

        context.getSource().getSender().sendMessage(tableHeader(idLength, backupLength));

        for (BackupObj backup : backups) {
            TextComponent component = backupOutput(backup, types, counter, idLength, backupLength);

            if (component != null) {
                context.getSource().getSender().sendMessage(component);
            }

            counter++;
        }
    }

    private static TextComponent tableHeader(int idLength, int backupLength) {
        String id = " ".repeat(TasksUtils.nonNegative(idLength - 2));
        String backup = " ".repeat(TasksUtils.nonNegative(backupLength - 4));

        return Component.text("ID" + id, NamedTextColor.DARK_GREEN)
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text("File" + backup, NamedTextColor.DARK_GREEN))
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text("Type", NamedTextColor.DARK_GREEN));
    }

    private static TextComponent backupOutput(BackupObj backup, List<TaskObj.Types> types, int counter, int idLength, int backupLength) {
        String idOffset = " ".repeat(TasksUtils.nonNegative(idLength - String.valueOf(counter).length()));
        String backupOffset = " ".repeat(TasksUtils.nonNegative(backupLength - backup.getFile().toPath().toString().length()));

        TextComponent component = Component.text(idOffset + counter, NamedTextColor.GREEN)
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text(backup.getFile().toString() + backupOffset, NamedTextColor.GREEN))
                .append(Component.text(" | ", NamedTextColor.WHITE))
                .append(Component.text(backup.getType().toString(), NamedTextColor.GREEN));

        if (types == null || types.contains(backup.getType())) {
            return component;
        }

        return null;
    }
}
