package org.diskium.management;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommandManagement {

    public static List<LiteralArgumentBuilder<CommandSourceStack>> configs() {

        List<LiteralArgumentBuilder<CommandSourceStack>> literals = new ArrayList<>();

        Map<String, Object> configs = ConfigManagement.getConfig();
        for (Map.Entry<String, Object> config : configs.entrySet()) {
            literals.add(
                    Commands.literal(config.getKey())
                            .executes(context -> {
                                Object value = ConfigManagement.getSingleConfig(config.getKey());
                                context.getSource().getSender().sendMessage("Configuration '" + config.getKey() + " = " + value + "'");
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(
                                    Commands.argument("value", BoolArgumentType.bool())
                                            .executes(context -> {
                                                boolean value = BoolArgumentType.getBool(context, "value");
                                                if (ConfigManagement.setSingleConfig(config.getKey(), value)) {
                                                    context.getSource().getSender().sendMessage("Set configuration '" + config.getKey() + "' to " + value);
                                                } else {
                                                    context.getSource().getSender().sendMessage("Unable to set configuration '" + config.getKey() + "'");
                                                }
                                                return Command.SINGLE_SUCCESS;
                                            })
                            )
            );
        }
        return literals;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> logsDates() {
        Commands.literal("since")
                .then(
                        Commands.argument("sinceArg", StringArgumentType.string())
                                .executes(context -> {
                                    String inputedDate = StringArgumentType.getString(context, "sinceArg");

                                })
                )
    }
}
