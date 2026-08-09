package org.diskium.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.diskium.management.ConfigManagement;

public class ConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> entry() { // TODO: Create CommandManager and create method returning the Commands (to prevent duplicating code)

        return Commands.literal("config")
                .then(
                        Commands.literal("bootstrap")
                                .executes(context -> {
                                    Object value = ConfigManagement.getSingleConfig("bootstrap");
                                    context.getSource().getSender().sendMessage("Configuration 'bootstrap = " + value + "'");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    if (ConfigManagement.setSingleConfig("bootstrap", value)) {
                                                        context.getSource().getSender().sendMessage("Set configuration 'bootstrap' to " + value);
                                                    } else {
                                                        context.getSource().getSender().sendMessage("Unable to set configuration 'bootstrap'");
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("backups")
                                .executes(context -> {
                                    Object value = ConfigManagement.getSingleConfig("backups");
                                    context.getSource().getSender().sendMessage("Config 'backups = " + value + "'");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    if (ConfigManagement.setSingleConfig("backups", value)) {
                                                        context.getSource().getSender().sendMessage("Set configuration 'bootstrap' to " + value);
                                                    } else {
                                                        context.getSource().getSender().sendMessage("Unable to set configuration 'backups'");
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                )
                .then(
                        Commands.literal("delete-logs-while-running")
                                .executes(context -> {
                                    Object value = ConfigManagement.getSingleConfig("delete-logs-while-running");
                                    context.getSource().getSender().sendMessage("Config 'delete-logs-while-running = " + value + "'");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    if (ConfigManagement.setSingleConfig("delete-logs-while-running", value)) {
                                                        context.getSource().getSender().sendMessage("Set configuration 'delete-logs-while-running' to " + value);
                                                    } else {
                                                        context.getSource().getSender().sendMessage("Unable to set configuration 'delete-logs-while-running'");
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }
}
