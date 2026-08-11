package org.diskium.management;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandManager {

    public LiteralArgumentBuilder<CommandSourceStack>[] configs() {

        List<LiteralArgumentBuilder<CommandSourceStack>> literals = new ArrayList<>();

        Map<String, Boolean> configs = ConfigManagement.getConfig();
        for (Map.Entry<String, Boolean> config : configs.entrySet()) {
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
            )
        }
    }
}
