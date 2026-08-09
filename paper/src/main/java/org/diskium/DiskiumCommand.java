package org.diskium;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.diskium.management.ConfigManagement;
import org.diskium.management.LogsManagement;
import org.diskium.management.PluginManagement;
import org.diskium.management.WorldManagement;

import java.util.Arrays;
import java.util.Map;

public class DiskiumCommand implements CommandExecutor { // TODO: Use Paper's command logic, not Bukkit's
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
        if (args[0].equalsIgnoreCase("config")) {
            if (args.length == 2) {
                sender.sendMessage(args[1] + ": " + ConfigManagement.getSingleConfig(args[1]).toString());
                return true;
            } else if (args.length > 2) {
                ConfigManagement.setSingleConfig(args[1], Boolean.getBoolean(args[2]));
                return true;
            }
            for (Map.Entry<String, Object> i : ConfigManagement.getConfig().entrySet()){
                sender.sendMessage(i.getValue() + ": " + i.getValue().toString());
            }
            sender.sendMessage("Use /diskium config <name> or /diskium config <name> <value> for printing and setting specific config value");
            return true;
        }
        else if (args[0].equalsIgnoreCase("logs")) {
            if (args.length >= 2){
                if (args[1].equalsIgnoreCase("list")){
                    if (args.length == 4){
                        sender.sendMessage(LogsManagement.getLogs(args[2], args[3]));
                        return true;
                    }
                    else if (args.length == 3){
                        sender.sendMessage(LogsManagement.getLogs(args[2], null));
                        return true;
                    }
                    sender.sendMessage(LogsManagement.getLogs(null, null));
                    return true;
                }
                else if (args[1].equalsIgnoreCase("delete")){
                    if (args.length == 3){
                        if (args[2].equalsIgnoreCase("all")){
                            LogsManagement.delete(null, null);
                            return true;
                        }
                        LogsManagement.delete(args[2], null);
                        return true;
                    }
                    if (args.length == 4){
                        LogsManagement.delete(args[2], args[3]);
                        return true;
                    }
                    sender.sendMessage("Use /diskium logs delete <all>/<startingDate> <endingDate>");
                }
                else if (args[1].equalsIgnoreCase("search")){
                    if (args.length == 2){
                        sender.sendMessage("Use /diskium logs search <keyword(s)>");
                        return true;
                    }
                    String keyword = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    Map<String, Integer> results = LogsManagement.search(keyword);
                    if (results.isEmpty()) sender.sendMessage("Couldn't find " + keyword + " in the logs.");
                    if (results == null) sender.sendMessage("Something went wrong while searching the logs.");
                    for (Map.Entry<String, Integer> entry : results.entrySet()){
                        sender.sendMessage("Found " + entry.getValue() + " matches in " + entry.getKey());
                    }
                    return true;
                }
            }
            sender.sendMessage("Use /diskium logs <list>/<delete>/<search>");
            return true;
        }
        else if (args[0].equalsIgnoreCase("plugin")) {
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("disable")){
                    if (args.length >= 3){
                        if (args[2].equalsIgnoreCase("thisinstance")){
                            if (args.length >= 4){
                                if (PluginManagement.tempDisablePlugin(args[3])){
                                    return true;
                                }
                                sender.sendMessage("Plugin " + args[3] + " does not exist");
                                return true;
                            }
                            sender.sendMessage("Use /diskium plugin disable thisInstance <pluginName>");
                            return true;
                        }
                        if (args[2].equalsIgnoreCase("untilmanualyenabled")){
                            if (args.length >= 4){
                                if (PluginManagement.permDisablePlugin(args[3])) return true;
                            }
                        }
                        sender.sendMessage("Use /diskium plugin disable untilManualyEnabled <pluginName>");
                    }
                    sender.sendMessage("Use /diskium plugin disable <thisInstance>/<untilManualyEnabled> <pluginName>");
                    return true;
                }
                else if (args[1].equalsIgnoreCase("enable")){
                    if (args.length >= 3) {
                        if (PluginManagement.tempEnablePlugin(args[2])) return true;
                        if (!PluginManagement.permEnablePlugin(args[2])) sender.sendMessage("Something went wrong while enabling " + args[2]);
                    }
                }
                else if (args[1].equalsIgnoreCase("delete")){
                    if (args.length >= 3) {
                        if (args[2].equalsIgnoreCase("folder")){
                            if (args.length >= 4){
                                PluginManagement.deleteFolder(args[3]);
                                return true;
                            }
                            sender.sendMessage("Use /diskium plugin delete folder <pluginName>");
                            return true;
                        }
                        else if (args[2].equalsIgnoreCase("plugin")){
                            if (args.length >= 4){
                                PluginManagement.deletePlugin(args[3]);
                                return true;
                            }
                            sender.sendMessage("Use /diskium plugin delete plugin <pluginName>");
                            return true;
                        }
                        else if (args[2].equalsIgnoreCase("folderandplugin")){
                            if (args.length >= 4){
                                PluginManagement.deletePlugin(args[3]);
                                PluginManagement.deleteFolder(args[3]);
                                return true;
                            }
                            sender.sendMessage("Use /diskium plugin delete folderAndPlugin <pluginName>");
                            return true;
                        }
                    }
                    sender.sendMessage("Use /diskium plugin delete <folder>/<plugin>/<folderAndPlugin> <pluginName>");
                    return true;
                }
                else if (args[1].equalsIgnoreCase("info")){
                    if (args.length >= 3) {
                        String out = PluginManagement.pluginInfo(args[2]);
                        if (out == null) sender.sendMessage("Plugin " + args[2] + " does not exist");
                        else sender.sendMessage(out);
                        return true;
                    }
                    sender.sendMessage("Use /diskium plugin info <pluginName>");
                }
                else if (args[1].equalsIgnoreCase("list")){
                    sender.sendMessage(PluginManagement.getPlugins());
                    return true;
                }
            }
            sender.sendMessage("Use /diskium plugin <disable>/<enable>/<delete>/<info>/<list>");
            return true;
        }
        else if(args[0].equalsIgnoreCase("world")){
            if (args.length >= 2 && (args[1].equalsIgnoreCase("allworlds") || WorldManagement.exists(args[1]))){
                if (args.length >= 3){
                    if (args[2].equalsIgnoreCase("getblock")){
                        if (args.length >= 4){
                            if (args[3].equalsIgnoreCase("thisWorld")){
                                if (args.length <= 7){
                                    sender.sendMessage("Use /diskium world " + args[1] + " getBlock thisWorld <x> <y> <z>");
                                    return true;
                                }
                                String toSend = WorldManagement.getBlock(args[1], args[3], args[4], args[5], true);
                                if (toSend == null) sender.sendMessage("Something went wrong");
                                else sender.sendMessage(toSend);
                                return true;
                            }
                            else if (args[3].equalsIgnoreCase("naturally")) {
                                if (args.length <= 7) {
                                    sender.sendMessage("Use /diskium world " + args[1] + " getBlock naturally <x> <y> <z>");
                                    return true;
                                }
                                String toSend = WorldManagement.getBlock(args[1], args[3], args[4], args[5], false);
                                if (toSend == null) sender.sendMessage("Something went wrong");
                                else sender.sendMessage(toSend);
                                return true;
                            }
                        }
                    }
                }
            }
            sender.sendMessage("Use /diskium world <allWorlds>/<specificWorld>");
        }
        return true;
    }
}
