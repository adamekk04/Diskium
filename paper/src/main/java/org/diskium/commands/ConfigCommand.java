package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.diskium.management.ConfigManagement;
import org.diskium.utils.DateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry(File dir) {

        List<LiteralArgumentBuilder<CommandSourceStack>> subcommands = configs(dir);
        LiteralArgumentBuilder<CommandSourceStack> configRoot = Commands.literal("config");

        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : subcommands) {
            configRoot.then(subcommand);
        }

        return configRoot;
    }

    private static List<LiteralArgumentBuilder<CommandSourceStack>> configs(File dir) {
        List<LiteralArgumentBuilder<CommandSourceStack>> literals = new ArrayList<>();
        Map<String, Object> configs = ConfigManagement.getConfig(dir);

        for (Map.Entry<String, Object> config : configs.entrySet()) {
            RequiredArgumentBuilder<CommandSourceStack, String> arg = Commands.argument("value", StringArgumentType.word())
                    .executes(context -> {
                        String value = StringArgumentType.getString(context, "value");

                        if (DateUtils.isValidString(value)) {
                            if (ConfigManagement.setSingleConfig(config.getKey(), value)) {
                                context.getSource().getSender().sendMessage(Component.text("Set configuration ").append(Component.text(config.getKey(), NamedTextColor.DARK_GREEN)).append(Component.text(" to ")).append(Component.text(value, NamedTextColor.GREEN)));
                            } else {
                                context.getSource().getSender().sendMessage("Cannot set config, because something went wrong." + NamedTextColor.RED);
                            }
                        } else {
                            context.getSource().getSender().sendMessage(Component.text("Invalid date!", NamedTextColor.RED));
                        }

                        return Command.SINGLE_SUCCESS;
                    });


            literals.add(
                    Commands.literal(config.getKey())
                            .executes(context -> {
                                Object value = ConfigManagement.getSingleConfig(config.getKey());
                                context.getSource().getSender().sendMessage(Component.text(config.getKey(), NamedTextColor.DARK_GREEN).append(Component.text(" = ", NamedTextColor.WHITE)).append(Component.text(value.toString(), NamedTextColor.GREEN)));
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(
                                    config.getKey().equalsIgnoreCase("date-format") ? arg :

                                            Commands.argument("value", BoolArgumentType.bool())
                                                    .executes(context -> {
                                                        boolean value = BoolArgumentType.getBool(context, "value");
                                                        if (ConfigManagement.setSingleConfig(config.getKey(), value)) {
                                                            context.getSource().getSender().sendMessage(Component.text("Set configuration ").append(Component.text(config.getKey(), NamedTextColor.DARK_GREEN)).append(Component.text(" to ")).append(Component.text(value, NamedTextColor.GREEN)));
                                                        } else {
                                                            context.getSource().getSender().sendMessage("Cannot set config, because something went wrong." + NamedTextColor.RED);
                                                        }
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                            )
            );
        }
        return literals;
    }
}
